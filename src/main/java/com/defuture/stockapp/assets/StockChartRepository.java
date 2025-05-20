package com.defuture.stockapp.assets;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StockChartRepository extends MongoRepository<StockChart, String> {
	Optional<StockChart> findTopByStockCodeOrderByDateDesc(String stockCode);

	List<StockChart> findByStockCodeOrderByDateAsc(String stockCode);

	void deleteByStockCodeAndDate(String stockCode, LocalDate date);
}
