package com.defuture.stockapp.assets;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PredictedPriceDAO {
	private final JdbcTemplate jdbc;
	
	public PredictedPriceDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<PredictedPriceDTO> mapper = (rs, rowNum) -> {
        PredictedPriceDTO dto = new PredictedPriceDTO();
        dto.setDate(rs.getObject("target_date", LocalDate.class));
        dto.setPredictedPrice(rs.getString("predicted_price"));
        return dto;
    };

    public boolean existsByStockCode(String stockCode) {
        String sql = "SELECT EXISTS(SELECT 1 FROM predicted_stock_prices WHERE stock_code = ?)";
        return jdbc.queryForObject(sql, Boolean.class, stockCode);
    }

    public List<PredictedPriceDTO> findAfter(String stockCode, LocalDate date) {
        String sql =
            "SELECT target_date::date, predicted_price " +
            "FROM predicted_stock_prices " +
            "WHERE stock_code = ? AND target_date > ? " +
            "ORDER BY target_date ASC";
        return jdbc.query(sql, mapper, stockCode, date);
    }
}
