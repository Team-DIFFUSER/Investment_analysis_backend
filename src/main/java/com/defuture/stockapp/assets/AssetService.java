package com.defuture.stockapp.assets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetService {
	private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Value("${kiwoom.appkey}")
	private String appKey;

	@Value("${kiwoom.secretkey}")
	private String secretKey;

	private final RestTemplate restTemplate;
	private String accessToken;

	@Value("${securities.api.base-url}")
	private String baseUrl;

	@Value("${securities.api.token-endpoint}")
	private String tokenEndpoint;

	@Value("${securities.api.account-endpoint}")
	private String accountEndpoint;

	@Value("${securities.api.chart-endpoint}")
	private String chartEndpoint;

	public AssetService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
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

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});
		Map<String, Object> body = response.getBody();

		this.accessToken = (String) body.get("token");
		return this.accessToken;
	}

	public AccountEvaluationResponseDTO getAccountEvaluation(String token) {
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
		ResponseEntity<AccountEvaluationResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, request, AccountEvaluationResponseDTO.class);

		AccountEvaluationResponseDTO json = response.getBody();

		return json;
	}

	public ChartResponseDTO getDailyChart(String token, String stockCode) {
		String url = baseUrl + chartEndpoint; // API URL 설정
		String contYn = "Y";
		String nextKey = "";
		List<ChartResponseDTO.ChartData> allData = new ArrayList<>();
		ChartResponseDTO combined = null;

		while ("Y".equals(contYn)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			    throw new RuntimeException("Sleep 중 인터럽트 발생", e);
			}
			
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

			ChartResponseDTO page = response.getBody();
			HttpHeaders rh = response.getHeaders();

			if (combined == null) {
				combined = new ChartResponseDTO();
				combined.setStockCode(page.getStockCode());
			}

			allData.addAll(page.getChartData());

			contYn = rh.getFirst("cont-yn");
			nextKey = rh.getFirst("next-key");
			if (contYn == null)
				contYn = "N";
			if (nextKey == null)
				nextKey = "";
		}

		combined.setChartData(allData);
		return combined;

	}
}