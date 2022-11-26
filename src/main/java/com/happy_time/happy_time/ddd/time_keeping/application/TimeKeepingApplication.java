package com.happy_time.happy_time.ddd.time_keeping.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.time_keeping.model.TimeKeeping;
import com.happy_time.happy_time.ddd.time_keeping.repository.ITimeKeepingRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class TimeKeepingApplication {

    @Autowired
    private ITimeKeepingRepository iTimeKeepingRepository;

    @Autowired
    private MongoTemplate mongoTemplate;


    public TimeKeeping getByTenant(String tenant_id) throws Exception {
        if (StringUtils.isBlank(tenant_id)) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        return mongoTemplate.findOne(query, TimeKeeping.class);
    }

    public TimeKeeping create(TimeKeeping time_keeping) {
        Long current_time = System.currentTimeMillis();
        time_keeping.setCreated_date(current_time);
        time_keeping.setLast_updated_date(current_time);
        return iTimeKeepingRepository.save(time_keeping);
    }

    public TimeKeeping update(TimeKeeping time_keeping) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(time_keeping.get_id()));
        query.addCriteria(Criteria.where("tenant_id").is(time_keeping.getTenant_id()));
        Boolean is_exists = mongoTemplate.exists(query, Agent.class);
        if(is_exists) {
            time_keeping.setLast_updated_date(current_time);
            return mongoTemplate.save(time_keeping, "time_keeping");
        }
        else return null;
    }
}
