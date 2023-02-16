package com.happy_time.happy_time.ddd.configuration.department.repository;

import com.happy_time.happy_time.ddd.configuration.department.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDepartmentRepository extends MongoRepository<Department, String> {
}
