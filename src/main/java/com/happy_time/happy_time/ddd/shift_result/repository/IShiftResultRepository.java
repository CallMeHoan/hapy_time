package com.happy_time.happy_time.ddd.shift_result.repository;

import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IShiftResultRepository extends MongoRepository<ShiftResult, String> {
}
