package com.defuture.stockapp.assets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

	private final AssetService assetService;

	public AssetController(AssetService assetService) {
		this.assetService = assetService;
	}

	@GetMapping("")
	public ResponseEntity<?> getAccountEvaluation() { // @RequestHeader("Authorization") String token
		String accessToken = assetService.getAccessToken();
		AccountEvaluationResponseDTO response = assetService.getAccountEvaluation(accessToken);
		System.out.println("assets");
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{stkCd}")
	public ResponseEntity<ChartResponseDTO> getDailyChart(@PathVariable("stkCd") String stockCode) { // @RequestHeader("Authorization") String token
		String accessToken = assetService.getAccessToken();
		ChartResponseDTO response = assetService.getDailyChart(accessToken, stockCode);

		return ResponseEntity.ok(response);
	}
}
