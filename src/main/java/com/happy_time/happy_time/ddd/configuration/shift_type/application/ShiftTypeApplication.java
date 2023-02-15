package com.happy_time.happy_time.ddd.configuration.shift_type.application;

import com.happy_time.happy_time.ddd.configuration.head_position_config.HeadPositionConfig;
import com.happy_time.happy_time.ddd.configuration.shift_type.ShiftType;
import com.happy_time.happy_time.ddd.configuration.shift_type.repository.IShiftTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShiftTypeApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IShiftTypeRepository shiftTypeRepository;

    public List<ShiftType> getAll() {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        return mongoTemplate.find(query, ShiftType.class);
    }

    public ShiftType create(ShiftType config) {
        Long current_time = System.currentTimeMillis();
        config.setCreated_date(current_time);
        config.setLast_updated_date(current_time);
        shiftTypeRepository.insert(config);
        return config;
    }
}
