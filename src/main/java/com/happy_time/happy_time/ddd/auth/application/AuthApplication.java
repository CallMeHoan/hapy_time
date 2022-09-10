package com.happy_time.happy_time.ddd.auth.application;

import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.tenant.application.TenantApplication;
import com.happy_time.happy_time.ddd.tenant.command.CommandCreateTenant;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthApplication {
    @Autowired
    private AgentApplication agentApplication;

    @Autowired
    private TenantApplication tenantApplication;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Boolean register(CommandRegister command) {
        CommandCreateTenant new_tenant = CommandCreateTenant.builder()
                .company_name(command.getCompany_name())
                .status("active")
                .scale(command.getScale())
                .build();

        Tenant tenant = this.tenantApplication.create(new_tenant).orElse(null);

        if(tenant == null) {
           return false;
        }

        Agent agent = Agent.builder()
                .tenant_id(tenant.get_id().toHexString())
                .user_name(command.getUser_name())
                .phone_number(command.getPhone_number())
                .personal_mail(command.getEmail())
                .build();

        Boolean created = agentApplication.create(agent);
        return created;
    }

}
