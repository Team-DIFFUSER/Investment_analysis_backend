package com.defuture.stockapp.users;

import lombok.Data;

@Data
public class InvestmentProfileResponseDTO {
    private String username;
    private double totalScore;
    private String investmentType;
}