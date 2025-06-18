package com.defuture.stockapp.assets;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StockInfoRepository extends MongoRepository<StockInfo, String>{
	Optional<StockInfo> findByName(String name);
}
