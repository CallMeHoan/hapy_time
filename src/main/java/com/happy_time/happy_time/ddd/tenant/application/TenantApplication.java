package com.happy_time.happy_time.ddd.tenant.application;

import com.happy_time.happy_time.ddd.tenant.command.CommandCreateTenant;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import com.happy_time.happy_time.ddd.tenant.repository.ITenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantApplication {

    @Autowired
    private ITenantRepository iTenantRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Optional<Tenant> create(CommandCreateTenant command) {
        Long current_time = System.currentTimeMillis();
        Tenant tenant = Tenant.builder()
                .company_name(command.getCompany_name())
                .status(command.getStatus())
                .scale(command.getScale())
                .is_deleted(false)
                .created_date(current_time)
                .last_updated_date(current_time)
                .build();

        Tenant created = iTenantRepository.save(tenant);
        return Optional.of(created);
    }
}
