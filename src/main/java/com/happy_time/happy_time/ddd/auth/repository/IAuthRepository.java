package com.happy_time.happy_time.ddd.auth.repository;

import com.happy_time.happy_time.ddd.auth.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuthRepository extends MongoRepository<Account, String> {
}
