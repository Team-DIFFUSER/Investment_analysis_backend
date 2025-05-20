package com.defuture.stockapp.assets;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "stock_charts")
@CompoundIndex(def = "{'stockCode':1,'date':1}", unique = true)
public class StockChart {
	@Id
	private String id;

	@Indexed
	private String stockCode;

	@Indexed
	private LocalDate date; // dt

	private String currentPrice; // cur_prc
	private String tradeQuantity; // trde_qty
	private String tradePriceAmount; // trde_prica
	private String openPrice; // open_pric
	private String highPrice; // high_pric
	private String lowPrice; // low_pric
}
