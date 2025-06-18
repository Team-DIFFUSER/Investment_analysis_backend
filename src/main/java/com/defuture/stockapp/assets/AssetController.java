package com.defuture.stockapp.assets;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

	private final AssetService assetService;
	private final PredictedPriceDAO predictedPriceDao;
	private final RecommendedStockDAO recommendedStockDAO;

	public AssetController(AssetService assetService, JdbcTemplate jdbc, PredictedPriceDAO predictedPriceDao, RecommendedStockDAO recommendedStockDAO) {
		this.assetService = assetService;
		this.predictedPriceDao = predictedPriceDao;
		this.recommendedStockDAO = recommendedStockDAO;
	}

	@GetMapping("")
	public ResponseEntity<AccountEvaluationResponseDTO> getAccountEvaluation(Authentication auth) { // @RequestHeader("Authorization") String token
		String accessToken = assetService.getAccessToken();
		AccountEvaluationResponseDTO response = assetService.getAccountEvaluation(accessToken, auth.getName());
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/charts")
	public ResponseEntity<UserChartResponseDTO> fetchUserChart(Authentication auth) { // @RequestHeader("Authorization") String token
		String accessToken = assetService.getAccessToken();
		UserChartResponseDTO response = assetService.fetchUserChart(accessToken, auth.getName());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{stkCd}")
	public ResponseEntity<ChartResponseDTO> getDailyChart(@PathVariable("stkCd") String stockCode) { // @RequestHeader("Authorization")
																										// String token
		String accessToken = assetService.getAccessToken();
		ChartResponseDTO response = assetService.getDailyChart(accessToken, stockCode);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{stkCd}/financials")
	public ResponseEntity<FinancialStatementDTO> getFinancialStatement(@PathVariable("stkCd") String stockCode) { // @RequestHeader("Authorization")
																													// String
																													// token
		String accessToken = assetService.getAccessToken();
		FinancialStatementDTO response = assetService.getFinancialStatement(accessToken, stockCode);

		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{stkCd}/predicted-prices")
    public ResponseEntity<List<PredictedPriceDTO>> getAllPredictions(@PathVariable("stkCd") String stockCode) {
		if (!predictedPriceDao.existsByStockCode(stockCode)) {
            return ResponseEntity.notFound().build();
        }
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<PredictedPriceDTO> list = predictedPriceDao.findAfter(stockCode, today.plusDays(1));
        return ResponseEntity.ok(list);
    }
	
	@GetMapping("/recommendations")
	public ResponseEntity<List<RecommendedStockDTO>> getRecommendedStocks() {
	    return ResponseEntity.ok(recommendedStockDAO.findAllRecommended());
	}
	
	@PostMapping("/stock-info")
	public ResponseEntity<String> syncAll(Authentication auth) { // @RequestHeader("Authorization") String token
		String accessToken = assetService.getAccessToken();
		try {
			assetService.fetchAndSaveAll(accessToken);
            return ResponseEntity.ok("종목정보 동기화 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("동기화 중 오류 발생: " + e.getMessage());
        }
	}
	
	@GetMapping("/code/{name}")
	public ResponseEntity<String> getCode(@PathVariable String name) {
		try {
            String code = assetService.getCodeByName(name);
            return ResponseEntity.ok(code);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(e.getMessage());
        }
	}
}
