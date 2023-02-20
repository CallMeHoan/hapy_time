package com.happy_time.happy_time.ddd.request_procedure.repository;

import com.happy_time.happy_time.ddd.request_procedure.RequestProcedure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRequestProcedureRepository extends MongoRepository<RequestProcedure, String> {
}
