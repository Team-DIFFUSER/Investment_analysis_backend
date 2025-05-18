package com.defuture.stockapp.news;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
public class NewsController {
	private final NewsService newsService;
	
	public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }
	
	@GetMapping("")
    public ResponseEntity<List<ArticleDTO>> searchNews(Authentication auth) {
		List<ArticleDTO> result = newsService.getCandidateArticles(auth.getName());
        return ResponseEntity.ok(result);
    }
}