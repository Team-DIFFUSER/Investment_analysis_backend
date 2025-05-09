package com.defuture.stockapp.users;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class InvestmentResponseDTO {
	private Integer questionId;
	
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)	//3번질문 중복 체크 허용
	private List<Integer> selectedOption;
}
