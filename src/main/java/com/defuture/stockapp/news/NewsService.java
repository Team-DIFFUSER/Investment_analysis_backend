package com.defuture.stockapp.news;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.defuture.stockapp.assets.AccountEvaluation.EvltData;
import com.defuture.stockapp.assets.AccountEvaluationRepository;
import com.defuture.stockapp.assets.StockChartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NewsService {
	private final RestTemplate restTemplate;
	private static final Duration STALE_THRESHOLD = Duration.ofHours(1);
    private static final List<String> BASE_KEYWORDS =
        List.of("경제","금리","환율","물가","gdp","실업률","통화정책");
    private static final int HOLDING_WEIGHT = 8;
    private static final int BASE_WEIGHT    = 2;
    private static final int SAMPLE_SIZE    = 50;
	
    private final CandidateArticleRepository candidateRepo;
    private final HoldingArticleRepository holdingArticleRepo;
    private final AccountEvaluationRepository actRepo;
    
	@Value("${naver.appkey}")
	private String appKey;

	@Value("${naver.secretkey}")
	private String secretKey;

	public NewsService(RestTemplate restTemplate, CandidateArticleRepository candidateRepo, HoldingArticleRepository holdingArticleRepo, AccountEvaluationRepository actRepo) {
        this.restTemplate = restTemplate;
		this.candidateRepo = candidateRepo;
		this.holdingArticleRepo = holdingArticleRepo;
		this.actRepo = actRepo;
    }
	
	public List<ArticleDTO> searchNews(String query) {
		String url = "https://openapi.naver.com/v1/search/news.json?query=" + query + "&display=100";
		HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", appKey);
        headers.set("X-Naver-Client-Secret", secretKey);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        Map<?,?> body = response.getBody();
        if (body == null) return Collections.emptyList();
        
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> items = (List<Map<String,Object>>) body.get("items");
        if (items == null) return Collections.emptyList();
        
		DateTimeFormatter rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME;
		List<ArticleDTO> result = new ArrayList<>();
		for (Map<String, Object> node : items) {
			String title = (String) node.get("title");
			String link = (String) node.get("link");
			String pubDate = (String) node.get("pubDate");

			ArticleDTO a = new ArticleDTO();
			a.setStockCode(query);
			a.setTitle(title);
			a.setUrl(link);
			a.setPubDate(OffsetDateTime.parse(pubDate, rfc1123).toInstant());
			a.setThumbnailUrl(""); // 나중에 채우기
			result.add(a);
		}
        return result;
	}
	
	public List<ArticleDTO> getCandidateArticles(String username) {
        // 1) 기존 데이터 조회
		Optional<CandidateArticle> opt = candidateRepo.findByUsername(username);
		if (opt.isEmpty() || opt.get().getLastUpdated().isBefore(Instant.now().minus(STALE_THRESHOLD))) {
            // 2) 보유종목 코드 추출 (evlData 안의 stockCode)
			List<String> holdingCodes = actRepo.findByUsername(username).get().getEvltData().stream()
					.map(EvltData::getStockCode).toList();

            // 3) 키워드 풀링 (중복 제거 Set)
            Set<ArticleDTO> pool = new HashSet<>();
            for (String keyword : BASE_KEYWORDS) {
                pool.addAll(searchNews(keyword));
            }
            for (String code : holdingCodes) {
                pool.addAll(searchNews(code.substring(1)));
            }

            // 4) 가중치 기반 랜덤 샘플 50개
            List<ArticleDTO> sampled = weightedSample(pool, holdingCodes);

            // 5) 썸네일 추출
            for (ArticleDTO article : sampled) {
            	article.setThumbnailUrl(fetchThumbnailUrl(article.getUrl()));
            }

            // 6) DB 갱신 (기존 삭제 후 저장)
            opt.ifPresent(old -> candidateRepo.deleteById(old.getId()));
            CandidateArticle doc = new CandidateArticle();
            doc.setUsername(username);
            doc.setLastUpdated(Instant.now());
            doc.setArticles(sampled);
            candidateRepo.save(doc);

            return sampled;
        }

        // 1시간 이내면 캐시 반환
        return opt.get().getArticles();
	}

	private List<ArticleDTO> weightedSample(Set<ArticleDTO> pool, List<String> holdingCodes) {
		List<ArticleDTO> picked = pool.stream().sorted(Comparator.comparingDouble((ArticleDTO article) -> {
			boolean isHolding = holdingCodes.contains(article.getStockCode());
			return isHolding ? HOLDING_WEIGHT : BASE_WEIGHT;
		}).reversed()).limit(50).toList();
		return picked;
	}
	
	private String fetchThumbnailUrl(String articleUrl) {
        try {
            Document doc = Jsoup.connect(articleUrl)
                                .userAgent("Mozilla/5.0 (jsoup)")
                                .timeout(5_000)
                                .get();
            Element meta = doc.selectFirst("meta[property=og:image]");
            if (meta != null) {
                String url = meta.attr("content").trim();
                if (!url.isEmpty()) return url;
            }
        } catch (IOException e) {
            // 로깅만, 빈 스트링 리턴
            System.err.println("썸네일 추출 실패: " + e.getMessage());
        }
        return "";
    }
}