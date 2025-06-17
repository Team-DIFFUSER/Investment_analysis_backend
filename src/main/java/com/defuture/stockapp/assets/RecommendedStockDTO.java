package com.defuture.stockapp.assets;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RecommendedStockDTO {
	@JsonProperty("stock_code")
    private String stockCode;

    @JsonProperty("stock_name")
    private String stockName;

    @JsonProperty("recommendation_reason")
    private String recommendationReason;
}
