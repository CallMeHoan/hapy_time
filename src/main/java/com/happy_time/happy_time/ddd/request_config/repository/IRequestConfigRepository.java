package com.happy_time.happy_time.ddd.request_config.repository;

import com.happy_time.happy_time.ddd.request_config.RequestConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRequestConfigRepository extends MongoRepository<RequestConfig, String> {
}
