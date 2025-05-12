package com.defuture.stockapp.assets;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChartResponseDTO {
	@JsonProperty("stk_cd") //종목코드
    private String stockCode;

    @JsonProperty("stk_dt_pole_chart_qry") //주식일봉차트조회
    private List<ChartData> chartData;

    @Data
    public static class ChartData {
        @JsonProperty("cur_prc") //현재가
        private String currentPrice;

        @JsonProperty("trde_qty") //거래량
        private String tradeQuantity;

        @JsonProperty("trde_prica") //거래대금
        private String tradePriceAmount;

        @JsonProperty("dt") //일자
        private String date;

        @JsonProperty("open_pric") //시가
        private String openPrice;

        @JsonProperty("high_pric") //고가
        private String highPrice;

        @JsonProperty("low_pric") //저가
        private String lowPrice;
    }
}
