package com.happy_time.happy_time.ddd.shift_type.repository;

import com.happy_time.happy_time.ddd.shift_type.ShiftType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IShiftTypeRepository extends MongoRepository<ShiftType, String> {
}
