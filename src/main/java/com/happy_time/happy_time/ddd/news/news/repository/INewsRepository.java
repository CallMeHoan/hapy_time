package com.happy_time.happy_time.ddd.news.news.repository;

import com.happy_time.happy_time.ddd.news.news.New;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface INewsRepository extends MongoRepository<New, String> {

}
