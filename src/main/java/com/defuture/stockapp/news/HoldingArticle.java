package com.defuture.stockapp.news;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "holding_articles")
public class HoldingArticle {
	@Id
	private String url;

	private String stockCode;
	private String title;
	private String description;
	private Instant pubDate;
}
