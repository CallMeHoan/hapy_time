package com.happy_time.happy_time.ddd.leave_policy.repository;

import com.happy_time.happy_time.ddd.leave_policy.LeavePolicy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILeavePolicyRepository extends MongoRepository<LeavePolicy, String> {
}
