package com.happy_time.happy_time.ddd.job.repository;

import com.happy_time.happy_time.ddd.job.JobModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IJobRepository extends MongoRepository<JobModel, String> {
}
