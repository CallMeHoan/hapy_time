package com.happy_time.happy_time.ddd.auth.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.auth.repository.IAuthRepository;
import com.happy_time.happy_time.ddd.configs.calendar_config.application.CalendarConfigApplication;
import com.happy_time.happy_time.ddd.configs.calendar_config.model.CalendarConfig;
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
    private CalendarConfigApplication calendarConfigApplication;

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
                .build();

        Agent created = agentApplication.create(agent);

        if(created == null) {
            return null;
        }

        //Tạo lịch chấm công default
        CalendarConfig config = CalendarConfig.builder()
                .tenant_id(tenant.get_id().toHexString())
                .name("Ca hành chính")
                .calendar_code("HCFull")
                .is_active(true)
                .total_recognition(1.0)
                .total_when_forget_check_out(0.5)
                .check_in_time(CalendarConfig.TimeRange.builder()
                        .from(CalendarConfig.Time.builder()
                                .hour(7)
                                .minute(0)
                                .build())
                        .to(CalendarConfig.Time.builder()
                                .hour(8)
                                .minute(0)
                                .build())
                        .build())
                .check_out_time(CalendarConfig.TimeRange.builder()
                        .from(CalendarConfig.Time.builder()
                                .hour(17)
                                .minute(30)
                                .build())
                        .to(CalendarConfig.Time.builder()
                                .hour(18)
                                .minute(30)
                                .build())
                        .build())
                .end_working_time(CalendarConfig.Time.builder().hour(17).minute(0).build())
                .start_working_time(CalendarConfig.Time.builder().hour(8).minute(0).build())
                .build();

        CalendarConfig calendar_config = calendarConfigApplication.create(config);
        if(calendar_config == null) {
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
