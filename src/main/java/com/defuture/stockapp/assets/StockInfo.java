package com.defuture.stockapp.assets;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "stockInfo")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StockInfo {
	@Id
    private String code;
    private String name;
}
