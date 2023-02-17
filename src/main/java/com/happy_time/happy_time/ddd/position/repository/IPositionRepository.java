package com.happy_time.happy_time.ddd.position.repository;

import com.happy_time.happy_time.ddd.position.Position;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPositionRepository  extends MongoRepository<Position, String> {
}
