package com.happy_time.happy_time.ddd.configuration.attendance.application;

import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.configuration.attendance.AttendanceConfig;
import com.happy_time.happy_time.ddd.configuration.attendance.repository.IAttendanceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class AttendanceConfigApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IAttendanceConfigRepository iAttendanceConfigRepository;

    public AttendanceConfig getByTenant(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        return mongoTemplate.findOne(query, AttendanceConfig.class);
    }

    public AttendanceConfig create(AttendanceConfig config) throws Exception {
        Long current_time = System.currentTimeMillis();
        config.setCreated_at(current_time);
        config.setLast_updated_at(current_time);
        iAttendanceConfigRepository.insert(config);
        return config;
    }

    public AttendanceConfig update(AttendanceConfig config) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(config.get_id()));
        query.addCriteria(Criteria.where("tenant_id").is(config.getTenant_id()));
        boolean is_exists = mongoTemplate.exists(query, AttendanceConfig.class);
        if (is_exists) {
            config.setLast_updated_at(current_time);
            return iAttendanceConfigRepository.save(config);
        }
        return null;
    }
}
