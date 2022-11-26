package com.happy_time.happy_time.ddd.time_keeping.repository;

import com.happy_time.happy_time.ddd.time_keeping.model.TimeKeeping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITimeKeepingRepository extends MongoRepository<TimeKeeping, String> {
}
