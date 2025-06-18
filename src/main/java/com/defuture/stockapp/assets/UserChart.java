package com.defuture.stockapp.assets;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "user_charts")
@Data
@CompoundIndex(def = "{'username':1,'date':1}", unique = true)
public class UserChart {
	@Id
	private String id;

	private String username;
    private LocalDate date;
    private String amount;
}
