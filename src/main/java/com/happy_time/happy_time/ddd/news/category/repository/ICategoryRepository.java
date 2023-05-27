package com.happy_time.happy_time.ddd.news.category.repository;

import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.news.category.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICategoryRepository extends MongoRepository<Category, String> {
}
