package com.defuture.stockapp.news;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "candidate_articles")
public class CandidateArticle {
	@Id
    private String id;

    private String username;
    private Instant lastUpdated;
    private List<ArticleDTO> articles;

    public CandidateArticle(String username, Instant lastUpdated, List<ArticleDTO> articles) {
        this.username      = username;
        this.lastUpdated = lastUpdated;
        this.articles    = articles;
    }
}
