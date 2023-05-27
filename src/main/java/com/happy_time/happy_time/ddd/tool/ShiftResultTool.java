package com.happy_time.happy_time.ddd.tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class ShiftResultTool {
    @Autowired
    private MongoTemplate mongoTemplate;


    public void deleteShiftResult(String tenant_id) {
        org.springframework.data.mongodb.core.query.Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        mongoTemplate.remove(query, "shift_result");

    }
}
