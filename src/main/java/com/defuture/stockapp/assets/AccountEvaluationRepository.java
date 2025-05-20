package com.defuture.stockapp.assets;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountEvaluationRepository extends MongoRepository<AccountEvaluation, String> {
	Optional<AccountEvaluation> findByUsername(String username);

	void deleteByUsername(String username);
}
