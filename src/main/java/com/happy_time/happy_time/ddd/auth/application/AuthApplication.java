package com.happy_time.happy_time.ddd.auth.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.agent.repository.IAgentRepository;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.auth.repository.IAuthRepository;
import com.happy_time.happy_time.ddd.tenant.application.TenantApplication;
import com.happy_time.happy_time.ddd.tenant.command.CommandCreateTenant;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class AuthApplication implements UserDetailsService {
    @Autowired
    private AgentApplication agentApplication;

    @Autowired
    private TenantApplication tenantApplication;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IAuthRepository iAuthRepository;
    public Account register(CommandRegister command) throws Exception {
        if (StringUtils.isBlank(command.getPhone_number()) || StringUtils.isBlank(command.getPassword())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        if (this.count(command.getPhone_number()) > 0L) {
            throw new Exception(ExceptionMessage.PHONE_EXIST);
        }
        CommandCreateTenant new_tenant = CommandCreateTenant.builder()
                .company_name(command.getCompany_name())
                .scale(command.getScale())
                .build();

        Tenant tenant = tenantApplication.create(new_tenant).orElse(null);

        if(tenant == null) {
           return null;
        }

        Agent agent = Agent.builder()
                .tenant_id(tenant.get_id().toHexString())
                .name(command.getName())
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
                .name(created.getName())
                .password(command.getPassword())
                .role("admin")
                .phone_number(created.getPhone_number())
                .build();

        return this.create(account);
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

    public Account findByPhoneNumber(String phone_number) {
        Query query = new Query();
        query.addCriteria(Criteria.where("phone_number").is(phone_number));
        return mongoTemplate.findOne(query, Account.class);
    }

    public Account getById (ObjectId id) {
        Account agent = mongoTemplate.findById(id, Account.class);
        if(agent != null) {
            if (agent.getIs_deleted()) return null;
            return agent;
        } else return null;
    }

    @Override
    public Account loadUserByUsername(String phone_number) throws UsernameNotFoundException {
        Account account = this.findByPhoneNumber(phone_number);
        if (account == null) {
            throw new UsernameNotFoundException("not found");
        }
        return account;
    }
}
