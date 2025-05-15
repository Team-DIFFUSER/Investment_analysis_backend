package com.defuture.stockapp.assets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssetService {
	private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final Duration STALE_THRESHOLD = Duration.ofHours(1);

	@Value("${kiwoom.appkey}")
	private String appKey;

	@Value("${kiwoom.secretkey}")
	private String secretKey;

	private final AccountEvaluationRepository actRepo;
	private final StockChartRepository stkRepo;
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
	
	@Value("${securities.api.stkInfo-endpoint}")
	private String stkInfoEndpoint;

	public AssetService(RestTemplate restTemplate, AccountEvaluationRepository actRepo, StockChartRepository stkRepo) {
		this.restTemplate = restTemplate;
		this.actRepo= actRepo;
		this.stkRepo= stkRepo;
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
	
	public AccountEvaluationResponseDTO getAccountEvaluation(String token, String username) {
        Optional<AccountEvaluation> act = actRepo.findByUsername(username);

        if (act.isEmpty() || isStale(act.get().getLastUpdated())) {
            act.ifPresent(e -> actRepo.deleteByUsername(e.getUsername()));

            AccountEvaluationResponseDTO freshDto = fetchFreshAccountEvaluation(token);

            AccountEvaluation entity = mapDtoToEntity(username, freshDto);
            entity.setLastUpdated(Instant.now());

            actRepo.save(entity);

            return freshDto;
        }

        return mapEntityToDto(act.get());
    }
	
	private boolean isStale(Instant lastUpdated) {
        return lastUpdated.plus(STALE_THRESHOLD)
                          .isBefore(Instant.now());
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
		ResponseEntity<AccountEvaluationResponseDTO> response = restTemplate.exchange(url, HttpMethod.POST, request, AccountEvaluationResponseDTO.class);

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
		Optional<StockChart> latestOpt = stkRepo.findTopByStockCodeOrderByDateDesc(stockCode);
		LocalDate latestDate = latestOpt.map(StockChart::getDate)
                .orElse(null);
		
		if (latestDate != null) {
			stkRepo.deleteByStockCodeAndDate(stockCode, latestDate);
        }
		
		List<ChartResponseDTO.ChartData> newData = new ArrayList<>();
        String contYn = "Y", nextKey = "";
        while ("Y".equals(contYn)) {
        	ChartResponseDTO page = fetchFullChart(token, stockCode, contYn, nextKey);
        	contYn   = page.getContYn();
            nextKey = page.getNextKey();
            for (var d : page.getChartData()) {
                if (latestDate == null || !d.getDate().isBefore(latestDate)) {
                	newData.add(d);
                }
                else {
                    contYn = "N";
                    break;
                }
            }
        }
        
        if (!newData.isEmpty()) {
            List<StockChart> toSave = newData.stream()
                .map(d -> {
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
                })
                .collect(Collectors.toList());
            stkRepo.saveAll(toSave);
        }
        
        List<StockChart> all = stkRepo.findByStockCodeOrderByDateAsc(stockCode);
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
		
		ResponseEntity<FinancialStatementDTO> response = restTemplate.exchange(url, HttpMethod.POST, request, FinancialStatementDTO.class);
		
		FinancialStatementDTO json = response.getBody();
		
		return json;
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

        List<AccountEvaluation.EvltData> list = dto.getEvltData().stream()
            .map(d -> {
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
            })
            .collect(Collectors.toList());

        act.setEvltData(list);
        return act;
    }
	
	private AccountEvaluationResponseDTO mapEntityToDto(AccountEvaluation act) {
        AccountEvaluationResponseDTO dto = new AccountEvaluationResponseDTO();
        dto.setEntr(act.getEntr());
        dto.setD2EntBalance(act.getD2EntBalance());
        dto.setTotalEstimate(act.getTotalEstimate());
        dto.setTotalLspftAmt(act.getTotalLspftAmt());
        dto.setEntr(act.getEntr());
        dto.setProfitLoss(act.getProfitLoss());
        dto.setProfitLossRate(act.getProfitLossRate());

        List<AccountEvaluationResponseDTO.EvltData> list = act.getEvltData().stream()
            .map(ed -> {
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
            })
            .collect(Collectors.toList());

        dto.setEvltData(list);
        return dto;
    }
}