package com.happy_time.happy_time.ddd.auth.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.command.CommandChangePassword;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.auth.repository.IAuthRepository;
import com.happy_time.happy_time.ddd.tenant.application.TenantApplication;
import com.happy_time.happy_time.ddd.tenant.command.CommandCreateTenant;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

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
        //Tạo doanh nghiệp
        CommandCreateTenant new_tenant = CommandCreateTenant.builder()
                .company_name(command.getCompany_name())
                .company_shorthand(command.getCompany_shorthand())
                .scale(command.getScale())
                .build();

        Tenant tenant = tenantApplication.create(new_tenant).orElse(null);

        if(tenant == null) {
           return null;
        }

        //Tạo nhân viên
        Agent agent = Agent.builder()
                .tenant_id(tenant.get_id().toHexString())
                .name(command.getName())
                .phone_number(command.getPhone_number())
                .personal_mail(command.getPersonal_mail())
                .agent_status(3)
                .agent_type(1)
                .role(1)
                .is_has_account(true)
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

    public Account create(Account account) {
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

    public Boolean forgetPassword(CommandChangePassword command) throws Exception {
        if (StringUtils.isBlank(command.getNew_password()) || StringUtils.isBlank(command.getPhone_number())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("phone_number").is(command.getPhone_number()));
        Account account = mongoTemplate.findOne(query, Account.class);
        if (account == null) {
            throw new Exception(ExceptionMessage.ACCOUNT_NOT_EXIST);
        }
        account.setPassword(command.getNew_password());
        account.setLast_updated_date(System.currentTimeMillis());
        Account updated = mongoTemplate.save(account, "accounts");
        return true;
    }

    @Override
    public Account loadUserByUsername(String phone_number) throws UsernameNotFoundException {
        Account account = this.findByPhoneNumber(phone_number);
        if (account == null) {
            throw new UsernameNotFoundException("not found");
        }
        return account;
    }

    public Boolean getIsUsedHappyTime(String phone_number) throws Exception {
        if(StringUtils.isBlank(phone_number)) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("phone_number").is(phone_number));
        Agent agent = mongoTemplate.findOne(query, Agent.class);
        if (agent != null) {
            return agent.getIs_has_account();
        }
        return false;
    }

    public Agent getAgentByPhoneNumber(String phone_number) throws Exception {
        if(StringUtils.isBlank(phone_number)) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("phone_number").is(phone_number));
        Agent agent = mongoTemplate.findOne(query, Agent.class);
        return agent;
    }
}
