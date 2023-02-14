package com.happy_time.happy_time.ddd.configuration.ip_config.repository;

import com.happy_time.happy_time.ddd.configuration.ip_config.IPConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IIpConfigRepository  extends MongoRepository<IPConfig, String> {
}
