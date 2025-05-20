package com.defuture.stockapp.news;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateArticleRepository extends MongoRepository<CandidateArticle, String> {
	Optional<CandidateArticle> findByUsername(String username);

	void deleteByUsername(String username);
}
