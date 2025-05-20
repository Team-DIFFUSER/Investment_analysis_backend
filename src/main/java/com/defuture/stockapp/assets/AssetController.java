package com.defuture.stockapp.assets;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

	private final AssetService assetService;
	private final JdbcTemplate jdbc;

	public AssetController(AssetService assetService, JdbcTemplate jdbc) {
		this.assetService = assetService;
		this.jdbc = jdbc;
	}

	@GetMapping("")
	public ResponseEntity<?> getAccountEvaluation(Authentication auth) { // @RequestHeader("Authorization") String token
		String accessToken = assetService.getAccessToken();
		AccountEvaluationResponseDTO response = assetService.getAccountEvaluation(accessToken, auth.getName());
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
	
	@GetMapping("/predicted-prices")		//test
    public ResponseEntity<List<Map<String, Object>>> getAllPredictions() {
        String sql = "SELECT * FROM public.predicted_stock_prices";
        List<Map<String,Object>> rows = jdbc.queryForList(sql);
        return ResponseEntity.ok(rows);
    }
}
