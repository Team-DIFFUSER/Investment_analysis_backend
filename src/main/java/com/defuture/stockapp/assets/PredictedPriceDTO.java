package com.defuture.stockapp.assets;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PredictedPriceDTO {
	private LocalDate date;
    private String predictedPrice;
}
