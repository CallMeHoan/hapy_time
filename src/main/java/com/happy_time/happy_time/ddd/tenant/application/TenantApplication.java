package com.happy_time.happy_time.ddd.tenant.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.tenant.command.CommandCreateTenant;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import com.happy_time.happy_time.ddd.tenant.repository.ITenantRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantApplication {

    @Autowired
    private ITenantRepository iTenantRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Optional<Tenant> create(CommandCreateTenant command) throws Exception {
        Long current_time = System.currentTimeMillis();
        if (StringUtils.isBlank(command.getCompany_name())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Tenant tenant = Tenant.builder()
                .company_name(command.getCompany_name())
                .scale(command.getScale())
                .is_deleted(false)
                .created_date(current_time)
                .last_updated_date(current_time)
                .build();

        Tenant created = iTenantRepository.save(tenant);
        return Optional.of(created);
    }

    public Tenant getById(ObjectId id) {
        Tenant tenant = mongoTemplate.findById(id, Tenant.class);
        if(tenant != null) {
            if (tenant.getIs_deleted()) return null;
            return tenant;
        } else return null;
    }

    public Tenant edit(Tenant tenant) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(tenant.get_id()));
        Boolean is_exists = mongoTemplate.exists(query, Agent.class);
        if(is_exists) {
            tenant.setLast_updated_date(current_time);
            return iTenantRepository.save(tenant);
        }
        else return null;
    }
}
