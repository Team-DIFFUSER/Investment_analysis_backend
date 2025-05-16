package com.defuture.stockapp.news;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldingArticleRepository extends MongoRepository<HoldingArticle, String>{
	Optional<HoldingArticle> findFirstByOrderByLastUpdatedDesc();
}
