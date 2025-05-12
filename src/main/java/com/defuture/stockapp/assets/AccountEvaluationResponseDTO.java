package com.defuture.stockapp.assets;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class AccountEvaluationResponseDTO {
	@JsonProperty("d2_entra") //D+2추정예수금
    private String d2EntBalance;
	
	@JsonProperty("tot_est_amt") //유가잔고평가
    private String totalEstimate;
	
	@JsonProperty("tot_pur_amt") //총매입금액
    private String totalPurchase;
	
	@JsonProperty("lspft") //누적투자손익
    private String profitLoss;
	
	@JsonProperty("lspft_rt") //누적손익률
    private String profitLossRate;
	
	@JsonProperty("stk_acnt_evlt_prst") // 종목별계좌평가현황
    private List<EvltData> evltData;
	
	@Data
    public static class EvltData {
		@JsonProperty("stk_cd") //종목코드
        private String stockCode;

        @JsonProperty("stk_nm") //종목명
        private String name;

        @JsonProperty("rmnd_qty") //보유수량
        private String quantity;

        @JsonProperty("avg_prc") //평균단가
        private String avgPrice;

        @JsonProperty("cur_prc") //현재가
        private String currentPrice;

        @JsonProperty("evlt_amt") //평가금액
        private String evalAmount;

        @JsonProperty("pl_amt") //손익금액
        private String plAmount;
        
        @JsonProperty("pl_rt") //손익률
        private String plRate;
	}
}