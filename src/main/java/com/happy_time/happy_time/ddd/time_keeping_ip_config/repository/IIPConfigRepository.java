package com.happy_time.happy_time.ddd.time_keeping_ip_config.repository;

import com.happy_time.happy_time.ddd.time_keeping_ip_config.model.IPConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IIPConfigRepository extends MongoRepository<IPConfig, String> {
}
