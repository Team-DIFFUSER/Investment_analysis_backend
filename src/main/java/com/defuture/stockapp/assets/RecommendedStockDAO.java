package com.defuture.stockapp.assets;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendedStockDAO {
	private final JdbcTemplate jdbc;

    public RecommendedStockDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<RecommendedStockDTO> mapper = (rs, rowNum) -> {
        RecommendedStockDTO dto = new RecommendedStockDTO();
        dto.setStockCode(rs.getString("stock_code"));
        dto.setStockName(rs.getString("stock_name"));
        dto.setRecommendationReason(rs.getString("recommendation_reason"));
        return dto;
    };
    public List<RecommendedStockDTO> findAllRecommended() {
        String sql = "SELECT stock_code, stock_name, recommendation_reason FROM stock_recommendations";
        return jdbc.query(sql, mapper);
    }
}
