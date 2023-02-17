package com.happy_time.happy_time.ddd.head_position_config.repository;

import com.happy_time.happy_time.ddd.head_position_config.HeadPositionConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IHeadPositionConfig extends MongoRepository<HeadPositionConfig, String> {
}
