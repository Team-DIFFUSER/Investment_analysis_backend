package com.defuture.stockapp.assets;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserChartRepository extends MongoRepository<UserChart, String>{
	Optional<UserChart> findTopByOrderByDateDesc();
	List<UserChart> findByUsernameOrderByDateAsc(String username);
	Optional<UserChart> findTopByUsernameOrderByDateDesc(String username);
	List<UserChart> findByUsernameAndDateGreaterThanEqualOrderByDateAsc(String username, LocalDate date);
}
