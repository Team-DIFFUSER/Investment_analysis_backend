package com.defuture.stockapp.assets;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChartResponseDTO {
	@JsonProperty("stk_cd")
    private String stockCode;

    @JsonProperty("stk_dt_pole_chart_qry")
    private List<ChartData> chartData;

    @Data
    public static class ChartData {
        @JsonProperty("cur_prc")
        private String currentPrice;

        @JsonProperty("trde_qty")
        private String tradeQuantity;

        @JsonProperty("trde_prica")
        private String tradePriceAmount;

        @JsonProperty("dt")
        private String date;

        @JsonProperty("open_pric")
        private String openPrice;

        @JsonProperty("high_pric")
        private String highPrice;

        @JsonProperty("low_pric")
        private String lowPrice;
    }
}
