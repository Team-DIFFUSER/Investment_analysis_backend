package com.defuture.stockapp.news;

import java.time.Instant;

import lombok.Data;

@Data
public class ArticleDTO {
	private String stockCode;
	private String title;
    private String url;
    private Instant pubDate;
    private String thumbnailUrl;
}
