package com.defuture.stockapp.assets;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FinancialStatementDTO {
	@JsonProperty("stk_cd")
	private String stockCode;

	private String per;

	private String roe;

	private String pbr;

	private String ev;

	private String bps;

	@JsonProperty("sale_amt")
	private String saleAmt;

	@JsonProperty("bus_pro")
	private String busPro;

	@JsonProperty("cup_nga")
	private String cupNga;

	private String cap;
}
