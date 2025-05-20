package com.defuture.stockapp.news;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "holding_articles")
public class HoldingArticle {
	@Id
	private String id;

	private String stockCode;
	private String title;
	private String description;
	@Indexed(unique = true)
	private String url;
	private Instant pubDate;
}
