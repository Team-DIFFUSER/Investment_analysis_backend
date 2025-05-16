package com.defuture.stockapp.news;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "holding_articles")
public class HoldingArticle {
	@Id
    private String id;

    private Instant lastUpdated;      // 최종 갱신 시각
    private List<ArticleDTO> articles; // 보유종목 관련 전체 기사(썸네일Url은 "")

    public HoldingArticle(Instant lastUpdated, List<ArticleDTO> articles) {
        this.lastUpdated = lastUpdated;
        this.articles    = articles;
    }
}
