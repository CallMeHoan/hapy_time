package com.happy_time.happy_time.ddd.auth.application;

import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.auth.repository.IAuthRepository;
import com.happy_time.happy_time.ddd.tenant.application.TenantApplication;
import com.happy_time.happy_time.ddd.tenant.command.CommandCreateTenant;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class AuthApplication {
    @Autowired
    private AgentApplication agentApplication;

    @Autowired
    private TenantApplication tenantApplication;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IAuthRepository iAuthRepository;

    public Account register(CommandRegister command) throws Exception {
        if (this.count(command.getPhone_number()) > 0L) {
            throw new Exception("phone_number_is_already_exist");
        }
        CommandCreateTenant new_tenant = CommandCreateTenant.builder()
                .company_name(command.getCompany_name())
                .status("active")
                .scale(command.getScale())
                .build();

        Tenant tenant = this.tenantApplication.create(new_tenant).orElse(null);

        if(tenant == null) {
           return null;
        }

        Agent agent = Agent.builder()
                .tenant_id(tenant.get_id().toHexString())
                .user_name(command.getUser_name())
                .phone_number(command.getPhone_number())
                .personal_mail(command.getEmail())
                .role("admin")
                .build();

        Agent created = agentApplication.create(agent);

        if(created == null) {
            return null;
        }

        Account account = Account.builder()
                .tenant_id(tenant.get_id().toHexString())
                .agent_id(agent.get_id().toHexString())
                .status("active")
                .user_name(created.getUser_name())
                .password(command.getPassword())
                .role("admin")
                .phone_number(created.getPhone_number())
                .build();

        Account create_account = this.create(account);
        return create_account;
    }

    private Account create(Account account) {
        Long current_time = System.currentTimeMillis();
        account.setIs_deleted(false);
        account.setCreated_date(current_time);
        account.setLast_updated_date(current_time);
        iAuthRepository.save(account);
        return account;
    }

    private Long count(String phone_number) {
        Query query = new Query();
        query.addCriteria(Criteria.where("phone_number").is(phone_number));
        Long count = mongoTemplate.count(query, Account.class);
        return count;
    }

}
