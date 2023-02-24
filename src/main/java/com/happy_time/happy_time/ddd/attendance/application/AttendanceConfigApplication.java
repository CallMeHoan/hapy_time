package com.happy_time.happy_time.ddd.attendance.application;

import com.happy_time.happy_time.ddd.attendance.AttendanceConfig;
import com.happy_time.happy_time.ddd.attendance.repository.IAttendanceConfigRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    public String getTenantAttendanceConfig(AttendanceConfig config) {
        List<AttendanceConfig.Module> modules = config.getModules();
        AttendanceConfig.Module module_in_use = modules.stream().filter(i -> BooleanUtils.isTrue(i.getIs_enabled())).findFirst().orElse(null);
        if (module_in_use != null) {
            if ("attendance_using_phone".equals(module_in_use.getName())) {
                List<AttendanceConfig.Function> functions = module_in_use.getFunctions();
                AttendanceConfig.Function function_in_use = functions.stream().filter(i -> BooleanUtils.isTrue(i.getIs_enabled())).findFirst().orElse(null);
                if (function_in_use != null) {
                    return function_in_use.getName();
                }
            } else {
                return module_in_use.getName();
            }
        }
        return "";
    }
}
