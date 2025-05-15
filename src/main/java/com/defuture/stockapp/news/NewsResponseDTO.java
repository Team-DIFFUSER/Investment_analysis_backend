package com.defuture.stockapp.news;

import java.util.List;

import lombok.Data;

@Data
public class NewsResponseDTO {
    private String lastBuildDate;
	
	private int total;
	
	private List<NewsData> items;
	
	@Data
    public static class NewsData {
		private String title;
		
		private String link;
		
		private String description;
	}
}
