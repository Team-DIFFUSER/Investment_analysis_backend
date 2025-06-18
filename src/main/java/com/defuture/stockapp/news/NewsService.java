package com.defuture.stockapp.news;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import com.defuture.stockapp.assets.AccountEvaluation.EvltData;
import com.defuture.stockapp.assets.AccountEvaluationRepository;

import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;

@Service
public class NewsService {
	private final RestTemplate restTemplate;
	private static final Duration STALE_THRESHOLD = Duration.ofHours(1);
	private static final List<String> BASE_KEYWORDS = List.of("경제", "금리", "환율", "물가", "gdp", "실업률", "통화정책");
	private static final double HOLDING_WEIGHT = 8.0;
	private static final double BASE_WEIGHT = 2.0;
	private static final int SAMPLE_SIZE = 30;
	private static final Period HISTORY_PERIOD = Period.ofMonths(6);
	private static final long MIN_INTERVAL_MS = 110;

	private final CandidateArticleRepository candidateRepo;
	private final HoldingArticleRepository holdingArticleRepo;
	private final AccountEvaluationRepository actRepo;

	@Value("${naver.appkey}")
	private String appKey;

	@Value("${naver.secretkey}")
	private String secretKey;

	public NewsService(RestTemplate restTemplate, CandidateArticleRepository candidateRepo,
			HoldingArticleRepository holdingArticleRepo, AccountEvaluationRepository actRepo) {
		this.restTemplate = restTemplate;
		this.candidateRepo = candidateRepo;
		this.holdingArticleRepo = holdingArticleRepo;
		this.actRepo = actRepo;
	}

	public List<ArticleDTO> searchNews(String query, int display, int start) {
		try {
			Thread.sleep(MIN_INTERVAL_MS);
		} catch (InterruptedException ignored) {
		}
		String url = "https://openapi.naver.com/v1/search/news.json?query=" + query + "&display=" + display + "&start="
				+ start;
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Naver-Client-Id", appKey);
		headers.set("X-Naver-Client-Secret", secretKey);

		HttpEntity<Void> request = new HttpEntity<>(headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		Map<String, Object> body = response.getBody();
		if (body == null)
			return Collections.emptyList();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
		if (items == null)
			return Collections.emptyList();

		DateTimeFormatter rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME;

		List<ArticleDTO> result = new ArrayList<>();
		for (Map<String, Object> node : items) {
			String title = (String) node.get("title");
			String desc = (String) node.get("description");
			String link = (String) node.get("link");
			String pubDate = (String) node.get("pubDate");

			ArticleDTO a = new ArticleDTO();
			if (query.matches("\\d+"))
				a.setStockCode(query);
			a.setTitle(Jsoup.parse(title).text());
			a.setDescription(Jsoup.parse(desc).text());
			a.setUrl(link);
			a.setPubDate(OffsetDateTime.parse(pubDate, rfc1123).toInstant());
			a.setThumbnailUrl(""); // 나중에 채우기
			result.add(a);
		}
		return result;
	}

	public void fetchHoldingNews(String username) {
		Instant cutoff = LocalDate.now().minus(HISTORY_PERIOD).atStartOfDay(ZoneId.systemDefault()).toInstant();
		holdingArticleRepo.deleteByPubDateBefore(username, cutoff);

		List<String> holdingCodes = actRepo.findByUsername(username).get().getEvltData().stream()
				.map(EvltData::getStockCode).toList();

		for (String code : holdingCodes) {
			String stock = code.substring(1);
			Set<String> seen = new HashSet<>();

			Instant lastStored = holdingArticleRepo.findTopByStockCodeOrderByPubDateDesc(stock)
					.map(HoldingArticle::getPubDate).orElse(Instant.EPOCH);
			Instant cutoffDate = LocalDate.now().minus(HISTORY_PERIOD).atStartOfDay(ZoneId.systemDefault()).toInstant();
			Instant threshold = lastStored.isAfter(cutoffDate) ? lastStored : cutoffDate;

			int start = 1;
			while (true) {
				List<ArticleDTO> page = searchNews(stock, 100, start);
				if (page.isEmpty())
					break;

				List<HoldingArticle> toSave = page.stream().filter(a -> a.getPubDate().isAfter(threshold))
						.filter(a -> seen.add(a.getUrl())).map(a -> {
							HoldingArticle ha = new HoldingArticle();
							ha.setStockCode(stock);
							ha.setTitle(a.getTitle());
							ha.setDescription(a.getDescription());
							ha.setUrl(a.getUrl());
							ha.setPubDate(a.getPubDate());
							return ha;
						}).toList();

				if (!toSave.isEmpty()) {
					holdingArticleRepo.saveAll(toSave);
				}
				start += 100;
				if (start > 1000 || page.get(page.size() - 1).getPubDate().isBefore(threshold) || page.size() < 100) {
					break;
				}

			}
		}
	}

	public List<ArticleDTO> getCandidateArticles(String username) {
		// 1) 기존 데이터 조회
		Optional<CandidateArticle> ca = candidateRepo.findByUsername(username);
		if (ca.isEmpty() || ca.get().getLastUpdated().isBefore(Instant.now().minus(STALE_THRESHOLD))) {
			// 2) 보유종목 코드 추출 (evlData 안의 stockCode)
			List<String> holdingCodes = actRepo.findByUsername(username).get().getEvltData().stream()
					.map(EvltData::getStockCode).toList();

			// 3) 키워드 풀링 (중복 제거 Set)
			Set<ArticleDTO> pool = new HashSet<>();
			for (String keyword : BASE_KEYWORDS) {
				pool.addAll(searchNews(keyword, 10, 1));
			}
			for (String code : holdingCodes) {
				pool.addAll(searchNews(code.substring(1), 10, 1));
			}

			// 4) 가중치 기반 랜덤 샘플 30개
			List<ArticleDTO> sampled = weightedSample(pool, holdingCodes);

			// 5) 썸네일 및 본문내용 추출
			for (ArticleDTO article : sampled) {
				article.setThumbnailUrl(fetchThumbnailUrl(article.getUrl()));
				article.setDescription(fetchFullText(article.getUrl()));
			}

			sampled = sampled.stream().filter(a -> a.getDescription() != null && !a.getDescription().isBlank()
					&& a.getThumbnailUrl() != null && !a.getThumbnailUrl().isBlank()).toList();

			// 6) DB 갱신 (기존 삭제 후 저장)
			ca.ifPresent(old -> candidateRepo.deleteById(old.getId()));
			CandidateArticle doc = new CandidateArticle();
			doc.setUsername(username);
			doc.setLastUpdated(Instant.now());
			doc.setArticles(sampled);
			candidateRepo.save(doc);

			return sampled;
		}

		// 1시간 이내면 캐시 반환
		return ca.get().getArticles();
	}

	private List<ArticleDTO> weightedSample(Set<ArticleDTO> pool, List<String> holdingCodes) {
		List<ArticleDTO> items = new ArrayList<>(pool);
		List<Double> weights = items.stream()
				.map(a -> holdingCodes.contains(a.getStockCode()) ? HOLDING_WEIGHT : BASE_WEIGHT)
				.collect(Collectors.toList());

		Random rnd = new Random();
		List<ArticleDTO> sample = new ArrayList<>();

		for (int i = 0; i < SAMPLE_SIZE && !items.isEmpty(); i++) {
			double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
			double r = rnd.nextDouble() * totalWeight;

			double acc = 0;
			for (int j = 0; j < items.size(); j++) {
				acc += weights.get(j);
				if (acc >= r) {
					sample.add(items.get(j));
					items.remove(j);
					weights.remove(j);
					break;
				}
			}
		}
		return sample;
	}

	private String fetchThumbnailUrl(String articleUrl) {
		try {
			Document doc = Jsoup.connect(articleUrl).userAgent("Mozilla/5.0 (jsoup)").timeout(5_000).get();
			Element meta = doc.selectFirst("meta[property=og:image]");
			if (meta != null) {
				String url = meta.attr("content").trim();
				if (!url.isEmpty())
					return url;
			}
		} catch (IOException e) {
			System.err.println("썸네일 추출 실패: " + e.getMessage());
		}
		return "";
	}

	private String fetchFullText(String articleUrl) {
		try {
			Document doc = Jsoup.connect(articleUrl).userAgent("Mozilla/5.0 (compatible)").timeout(5_000).get();
			String html = doc.html();

			Readability4J readability = new Readability4J(articleUrl, html);
			Article article = readability.parse();

			String articleHtml = article.getContent();
			if (articleHtml == null || articleHtml.isBlank()) {
				Element body = doc.getElementById("newsct_article");
				if (body != null && !body.text().isBlank()) {
					return body.text().trim();
				}
				return "";
			}
			return Jsoup.parse(articleHtml).text().trim();

		} catch (IOException e) {
			System.err.println("본문 크롤링 실패" + e.getMessage());
		}
		return "";
	}
}