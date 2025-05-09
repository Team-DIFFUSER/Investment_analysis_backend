package com.defuture.stockapp.news;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
public class NewsController {
	private final NewsService newsService;
	
	public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }
	
	@GetMapping("")
    public ResponseEntity<?> searchNews(@RequestParam("query") String query) {
        NewsResponseDTO result = newsService.searchNews(query);
        return ResponseEntity.ok(result);
    }
}