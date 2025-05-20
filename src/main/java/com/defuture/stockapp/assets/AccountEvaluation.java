package com.defuture.stockapp.assets;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
@Document("user_holdings")
public class AccountEvaluation {
	@Id
	private String id;

	private String username;

	private Instant lastUpdated; // 최종 갱신 시각

	private String entr; // 예수금

	private String d2EntBalance; // D+2추정예수금

	private String totalEstimate; // 유가잔고평가

	private String totalPurchase; // 총매입금액

	private String totalLspftAmt; // 누적투자원금

	private String profitLoss; // 누적투자손익

	private String profitLossRate; // 누적손익률

	private List<EvltData> evltData; // 종목별계좌평가현황

	@Data
	public static class EvltData {
		private String stockCode; // 종목코드

		private String name; // 종목명

		private String quantity; // 보유수량

		private String avgPrice; // 평균단가

		private String currentPrice; // 현재가

		private String evalAmount; // 평가금액

		private String plAmount; // 손익금액

		private String plRate; // 손익률
	}
}
