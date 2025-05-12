package com.defuture.stockapp.news;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewsResponseDTO {
	private String lastBuildDate;
	private int total;
	private List<NewsItemDTO> items;
}
