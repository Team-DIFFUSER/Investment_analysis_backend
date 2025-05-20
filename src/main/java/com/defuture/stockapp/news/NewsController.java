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
	public ResponseEntity<List<ArticleDTO>> getCandidateArticles(Authentication auth) {
		List<ArticleDTO> result = newsService.getCandidateArticles(auth.getName());
		return ResponseEntity.ok(result);
	}

	@PostMapping("")
	public ResponseEntity<Void> fetchHoldingNews(Authentication auth) {
		newsService.fetchHoldingNews(auth.getName());
		return ResponseEntity.noContent().build();
	}
}