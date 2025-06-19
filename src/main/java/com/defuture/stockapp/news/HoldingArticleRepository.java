package com.defuture.stockapp.news;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldingArticleRepository extends MongoRepository<HoldingArticle, String> {
	void deleteByPubDateBefore(Instant date);

	Optional<HoldingArticle> findTopByStockCodeOrderByPubDateDesc(String stockCode);
}
