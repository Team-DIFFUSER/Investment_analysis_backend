package com.defuture.stockapp.news;

import java.util.List;

import lombok.Data;

@Data
public class NewsResponseDTO {
    private String lastBuildDate;
	
	private int total;
	
	private List<newsData> items;
	
	@Data
    public static class newsData {
		private String title;
		
		private String link;
		
		private String description;
	}
}
