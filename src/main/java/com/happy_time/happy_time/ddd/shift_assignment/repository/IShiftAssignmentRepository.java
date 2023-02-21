package com.happy_time.happy_time.ddd.shift_assignment.repository;

import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IShiftAssignmentRepository extends MongoRepository<ShiftAssignment, String> {
}
