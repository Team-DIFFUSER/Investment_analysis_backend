package com.defuture.stockapp.news;

import java.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "url")
public class ArticleDTO {
	private String stockCode;
	private String title;
	private String description;
	private String url;
	private Instant pubDate;
	private String thumbnailUrl;
}
