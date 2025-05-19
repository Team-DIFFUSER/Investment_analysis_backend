package com.defuture.stockapp.news;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldingArticleRepository extends MongoRepository<HoldingArticle, String>{
	void deleteByPubDateBefore(String username, Instant date);

    List<HoldingArticle> findByPubDateAfter(String username, Instant date);
}
