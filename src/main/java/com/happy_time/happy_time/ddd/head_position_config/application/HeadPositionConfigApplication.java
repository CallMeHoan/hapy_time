package com.happy_time.happy_time.ddd.head_position_config.application;

import com.happy_time.happy_time.ddd.head_position_config.HeadPositionConfig;
import com.happy_time.happy_time.ddd.head_position_config.repository.IHeadPositionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HeadPositionConfigApplication {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IHeadPositionConfig headPositionConfig;

    public List<HeadPositionConfig> getAll() {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        return mongoTemplate.find(query, HeadPositionConfig.class);
    }

    public HeadPositionConfig create(HeadPositionConfig config) {
        Long current_time = System.currentTimeMillis();
        config.setCreated_at(current_time);
        config.setLast_updated_at(current_time);
        headPositionConfig.insert(config);
        return config;
    }
}
