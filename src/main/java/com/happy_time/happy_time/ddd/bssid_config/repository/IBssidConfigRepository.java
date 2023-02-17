package com.happy_time.happy_time.ddd.bssid_config.repository;

import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBssidConfigRepository extends MongoRepository<BSSIDConfig, String> {
}
