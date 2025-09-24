package com.defuture.stockapp.assets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@Service
public class AssetService {
	private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final Duration STALE_THRESHOLD = Duration.ofHours(1);

	@Value("${kiwoom.appkey}")
	private String appKey;

	@Value("${kiwoom.secretkey}")
	private String secretKey;

	private final AccountEvaluationRepository accountEvaluationRepository;
	private final StockChartRepository stockChartRepository;
	private final RestTemplate restTemplate;
	private final UserChartRepository userChartRepository;
	private final StockInfoRepository stockInfoRepository;
	private String accessToken;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final List<String> MARKET_TYPES = Arrays.asList("0", // 코스피
			"10", // 코스닥
			"3", // ELW
			"8", // ETF
			"30", // K-OTC
			"50", // 코넥스
			"5", // 신주인수권
			"4", // 뮤추얼펀드
			"6", // 리츠
			"9" // 하이일드
	);

	@Value("${securities.api.base-url}")
	private String baseUrl;

	@Value("${securities.api.token-endpoint}")
	private String tokenEndpoint;

	@Value("${securities.api.account-endpoint}")
	private String accountEndpoint;

	@Value("${securities.api.chart-endpoint}")
	private String chartEndpoint;

	@Value("${securities.api.stkInfo-endpoint}")
	private String stkInfoEndpoint;

	public AssetService(RestTemplate restTemplate, AccountEvaluationRepository accountEvaluationRepository, StockChartRepository stockChartRepository,
			UserChartRepository userChartRepository, StockInfoRepository stockInfoRepository) {
		this.restTemplate = restTemplate;
		this.accountEvaluationRepository = accountEvaluationRepository;
		this.stockChartRepository = stockChartRepository;
		this.userChartRepository = userChartRepository;
		this.stockInfoRepository = stockInfoRepository;
	}

	public String getAccessToken() {
		String url = baseUrl + tokenEndpoint; // API URL 설정

		// 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 요청 바디(JSON 데이터)
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("grant_type", "client_credentials");
		requestBody.put("appkey", appKey); // kiwoom rest api appkey
		requestBody.put("secretkey", secretKey); // kiwoom rest api secretkey

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		Map<String, Object> body = response.getBody();

		this.accessToken = (String) body.get("token");
		return this.accessToken;
	}

	public AccountEvaluationResponseDTO getAccountEvaluation(String token, String username) {
		Optional<AccountEvaluation> optionalEvaluation = accountEvaluationRepository.findByUsername(username);
		if (optionalEvaluation.isEmpty() || isStale(optionalEvaluation.get().getLastUpdated())) {
			optionalEvaluation.ifPresent(e -> accountEvaluationRepository.deleteByUsername(e.getUsername()));

			AccountEvaluationResponseDTO freshDto = fetchFreshAccountEvaluation(token);

			AccountEvaluation entity = mapDtoToEntity(username, freshDto);
			entity.setLastUpdated(Instant.now());

			accountEvaluationRepository.save(entity);

			return freshDto;
		}

		return mapEntityToDto(optionalEvaluation.get());
	}

	private boolean isStale(Instant lastUpdated) {
		return lastUpdated.plus(STALE_THRESHOLD).isBefore(Instant.now());
	}

	public AccountEvaluationResponseDTO fetchFreshAccountEvaluation(String token) {
		String url = baseUrl + accountEndpoint; // API URL 설정
		
		// 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token); // "Authorization: Bearer {token}"
		headers.add("cont-yn", "N"); // 연속조회 여부
		headers.add("next-key", ""); // 연속조회 키
		headers.add("api-id", "kt00004"); // TR명

		// 요청 바디(JSON 데이터)
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("qry_tp", "0");
		requestBody.put("dmst_stex_tp", "KRX");

		// HTTP 요청 생성
		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		// POST 요청 실행
		ResponseEntity<AccountEvaluationResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, request,
				AccountEvaluationResponseDTO.class);

		AccountEvaluationResponseDTO json = response.getBody();
		return json;
	}

	public ChartResponseDTO fetchFullChart(String token, String stockCode, String contYn, String nextKey) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Sleep 중 인터럽트 발생", e);
		}

		String url = baseUrl + chartEndpoint; // API URL 설정

		// 요청 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token); // "Authorization: Bearer {token}"
		headers.add("cont-yn", contYn); // 연속조회 여부
		headers.add("next-key", nextKey); // 연속조회 키
		headers.add("api-id", "ka10081"); // TR명

		// 요청 바디(JSON 데이터)
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("stk_cd", stockCode);
		requestBody.put("base_dt", LocalDate.now().format(DF));
		requestBody.put("upd_stkpc_tp", "1");

		// HTTP 요청 생성
		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<ChartResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, request,
				ChartResponseDTO.class);

		ChartResponseDTO dto = response.getBody();
		HttpHeaders rh = response.getHeaders();
		String newContYn = rh.getFirst("cont-yn");
		String newNextKey = rh.getFirst("next-key");
		dto.setContYn(newContYn != null ? newContYn : "N");
		dto.setNextKey(newNextKey != null ? newNextKey : "");
		return dto;

	}

	public ChartResponseDTO getDailyChart(String token, String stockCode) {
		Optional<StockChart> latestOpt = stockChartRepository.findTopByStockCodeOrderByDateDesc(stockCode);
		LocalDate latestDate = latestOpt.map(StockChart::getDate).orElse(null);

		if (latestDate != null) {
			stockChartRepository.deleteByStockCodeAndDate(stockCode, latestDate);
		}

		List<ChartResponseDTO.ChartData> newData = new ArrayList<>();
		String contYn = "Y", nextKey = "";
		while ("Y".equals(contYn)) {
			ChartResponseDTO page = fetchFullChart(token, stockCode, contYn, nextKey);
			contYn = page.getContYn();
			nextKey = page.getNextKey();
			for (var d : page.getChartData()) {
				if (latestDate == null || !d.getDate().isBefore(latestDate)) {
					newData.add(d);
				} else {
					contYn = "N";
					break;
				}
			}
		}

		if (!newData.isEmpty()) {
			List<StockChart> toSave = newData.stream().map(d -> {
				StockChart sc = new StockChart();
				sc.setStockCode(stockCode);
				sc.setDate(d.getDate());
				sc.setCurrentPrice(d.getCurrentPrice());
				sc.setTradeQuantity(d.getTradeQuantity());
				sc.setTradePriceAmount(d.getTradePriceAmount());
				sc.setOpenPrice(d.getOpenPrice());
				sc.setHighPrice(d.getHighPrice());
				sc.setLowPrice(d.getLowPrice());
				return sc;
			}).collect(Collectors.toList());
			stockChartRepository.saveAll(toSave);
		}

		List<StockChart> all = stockChartRepository.findByStockCodeOrderByDateAsc(stockCode);
		ChartResponseDTO result = new ChartResponseDTO();
		result.setStockCode(stockCode);
		result.setChartData(all.stream().map(e -> {
			ChartResponseDTO.ChartData cd = new ChartResponseDTO.ChartData();
			cd.setDate(e.getDate());
			cd.setCurrentPrice(e.getCurrentPrice());
			cd.setTradeQuantity(e.getTradeQuantity());
			cd.setTradePriceAmount(e.getTradePriceAmount());
			cd.setOpenPrice(e.getOpenPrice());
			cd.setHighPrice(e.getHighPrice());
			cd.setLowPrice(e.getLowPrice());
			return cd;
		}).collect(Collectors.toList()));

		return result;
	}

	public FinancialStatementDTO getFinancialStatement(String token, String stockCode) {
		String url = baseUrl + stkInfoEndpoint;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token); // "Authorization: Bearer {token}"
		headers.add("cont-yn", "N"); // 연속조회 여부
		headers.add("next-key", ""); // 연속조회 키
		headers.add("api-id", "ka10001"); // TR명

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("stk_cd", stockCode);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<FinancialStatementDTO> response = restTemplate.exchange(url, HttpMethod.POST, request,
				FinancialStatementDTO.class);

		FinancialStatementDTO json = response.getBody();

		return json;
	}

	public UserChartResponseDTO fetchUserChart(String token, String username) {
		// 시작일 결정: DB에 데이터 있으면 마지막+1, 없으면 2020-01-01
		LocalDate startDate = userChartRepository.findTopByUsernameOrderByDateDesc(username).map(doc -> doc.getDate().plusDays(1))
				.orElse(LocalDate.of(2020, 1, 1));
		
		LocalDate endDate = LocalDate.now();
		String startDt = startDate.format(DF);
		String endDt = endDate.format(DF);

		String contYn = "N";
		String nextKey = "";

		List<UserChartResponseDTO.DepositData> buffer = new ArrayList<>();

		do {
			String url = baseUrl + accountEndpoint;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(token); // "Authorization: Bearer {token}"
			headers.add("cont-yn", contYn); // 연속조회 여부
			headers.add("next-key", nextKey); // 연속조회 키
			headers.add("api-id", "kt00002"); // TR명

			Map<String, String> requestBody = new HashMap<>();
			requestBody.put("start_dt", startDt);
			requestBody.put("end_dt", endDt);

			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

			ResponseEntity<UserChartResponseDTO> resp = restTemplate.exchange(url, HttpMethod.POST, request,
					UserChartResponseDTO.class);

			UserChartResponseDTO page = resp.getBody();
			HttpHeaders rh = resp.getHeaders();
			contYn = rh.getFirst("cont-yn");
			nextKey = rh.getFirst("next-key");

			List<UserChartResponseDTO.DepositData> pageData = page.getData();
			if (pageData != null && !pageData.isEmpty()) {
				buffer.addAll(pageData);
			}
		} while ("Y".equalsIgnoreCase(contYn));

		LocalDate lastSaved = userChartRepository.findTopByUsernameOrderByDateDesc(username).map(UserChart::getDate).orElse(null);

		List<UserChart> toSave = buffer.stream().filter(d -> lastSaved == null || d.getDate().isAfter(lastSaved))
				.map(d -> {
					UserChart doc = new UserChart();
					doc.setUsername(username);
					doc.setDate(d.getDate());
					doc.setAmount(d.getAmount());
					return doc;
				}).collect(Collectors.toList());

		if (!toSave.isEmpty()) {
			userChartRepository.saveAll(toSave);
		}
		// List<UserChart> all =
		// userChartRepository.findByUsernameAndDateGreaterThanEqualOrderByDateAsc(username,
		// startDate);
		List<UserChart> all = userChartRepository.findByUsernameOrderByDateAsc(username);
		List<UserChartResponseDTO.DepositData> result = all.stream()
				.map(u -> new UserChartResponseDTO.DepositData(u.getDate(), u.getAmount())).toList();
		return new UserChartResponseDTO(result);
	}

	public void syncAllStockInfo(String token) throws Exception {
		String url = baseUrl + stkInfoEndpoint;
		List<StockInfo> buffer = new ArrayList<>();

		for (String mrktTp : MARKET_TYPES) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Sleep 중 인터럽트 발생", e);
			}
			String contYn = "N";
			String nextKey = "";
			do {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setBearerAuth(token);
				headers.add("api-id", "ka10099");
				headers.add("cont-yn", contYn);
				headers.add("next-key", nextKey);

				String jsonBody = String.format("{\"mrkt_tp\":\"%s\"}", mrktTp);
				HttpEntity<String> req = new HttpEntity<>(jsonBody, headers);

				ResponseEntity<String> resp = restTemplate.postForEntity(url, req, String.class);

				if (!resp.getStatusCode().is2xxSuccessful()) {
					throw new RuntimeException("API 호출 실패: " + resp.getStatusCode());
				}

				JsonNode root = objectMapper.readTree(resp.getBody());
				JsonNode list = root.path("list");
				contYn = root.path("cont-yn").asText();
				nextKey = root.path("next-key").asText();
				for (JsonNode node : list) {
					buffer.add(StockInfo.builder().code(node.path("code").asText()).name(node.path("name").asText())
							.build());
				}
			} while ("Y".equals(contYn));
		}

		stockInfoRepository.saveAll(buffer);
	}
	
	public String getCodeByName(String name) {
        return stockInfoRepository.findByName(name)
            .map(StockInfo::getCode)
            .orElseThrow(() ->
                new NoSuchElementException("종목명 '" + name + "' 에 해당하는 코드가 없습니다.")
            );
    }

	private AccountEvaluation mapDtoToEntity(String username, AccountEvaluationResponseDTO dto) {
		AccountEvaluation act = new AccountEvaluation();
		act.setUsername(username);
		act.setEntr(dto.getEntr());
		act.setD2EntBalance(dto.getD2EntBalance());
		act.setTotalEstimate(dto.getTotalEstimate());
		act.setTotalPurchase(dto.getTotalPurchase());
		act.setTotalLspftAmt(dto.getTotalLspftAmt());
		act.setProfitLoss(dto.getProfitLoss());
		act.setProfitLossRate(dto.getProfitLossRate());

		List<AccountEvaluation.EvltData> list = dto.getEvltData().stream().map(d -> {
			AccountEvaluation.EvltData ed = new AccountEvaluation.EvltData();
			ed.setStockCode(d.getStockCode());
			ed.setName(d.getName());
			ed.setQuantity(d.getQuantity());
			ed.setAvgPrice(d.getAvgPrice());
			ed.setCurrentPrice(d.getCurrentPrice());
			ed.setEvalAmount(d.getEvalAmount());
			ed.setPlAmount(d.getPlAmount());
			ed.setPlRate(d.getPlRate());
			return ed;
		}).collect(Collectors.toList());

		act.setEvltData(list);
		return act;
	}

	private AccountEvaluationResponseDTO mapEntityToDto(AccountEvaluation act) {
		AccountEvaluationResponseDTO dto = new AccountEvaluationResponseDTO();
		dto.setEntr(act.getEntr());
		dto.setD2EntBalance(act.getD2EntBalance());
		dto.setTotalEstimate(act.getTotalEstimate());
		dto.setTotalPurchase(act.getTotalPurchase());
		dto.setTotalLspftAmt(act.getTotalLspftAmt());
		dto.setProfitLoss(act.getProfitLoss());
		dto.setProfitLossRate(act.getProfitLossRate());

		List<AccountEvaluationResponseDTO.EvltData> list = act.getEvltData().stream().map(ed -> {
			AccountEvaluationResponseDTO.EvltData d = new AccountEvaluationResponseDTO.EvltData();
			d.setStockCode(ed.getStockCode());
			d.setName(ed.getName());
			d.setQuantity(ed.getQuantity());
			d.setAvgPrice(ed.getAvgPrice());
			d.setCurrentPrice(ed.getCurrentPrice());
			d.setEvalAmount(ed.getEvalAmount());
			d.setPlAmount(ed.getPlAmount());
			d.setPlRate(ed.getPlRate());
			return d;
		}).collect(Collectors.toList());

		dto.setEvltData(list);
		return dto;
	}
}