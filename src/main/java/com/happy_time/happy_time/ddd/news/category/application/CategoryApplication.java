package com.happy_time.happy_time.ddd.news.category.application;

import com.happy_time.happy_time.ddd.news.category.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class CategoryApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ICategoryRepository iCategoryRepository;
}
