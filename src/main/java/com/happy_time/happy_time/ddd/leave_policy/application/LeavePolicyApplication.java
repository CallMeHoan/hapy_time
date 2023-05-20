package com.happy_time.happy_time.ddd.leave_policy.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.leave_policy.LeavePolicy;
import com.happy_time.happy_time.ddd.leave_policy.command.CommandLeavePolicy;
import com.happy_time.happy_time.ddd.leave_policy.repository.ILeavePolicyRepository;
import com.happy_time.happy_time.ddd.request_config.RequestConfig;
import com.happy_time.happy_time.ddd.request_config.command.CommandRequestConfig;
import com.twilio.twiml.voice.Leave;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class LeavePolicyApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ILeavePolicyRepository leavePolicyRepository;

    public LeavePolicy create(LeavePolicy policy) {
        Long current_time = System.currentTimeMillis();
        policy.setCreated_date(current_time);
        policy.setLast_updated_date(current_time);
        return leavePolicyRepository.insert(policy);
    }


    public LeavePolicy clone(ReferenceData ref, String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(AppConstant.TENANT_DEFAULT));
        LeavePolicy policy = mongoTemplate.findOne(query, LeavePolicy.class);
        Long current_time = System.currentTimeMillis();
        if (policy != null) {
            policy.set_id(null);
            policy.setTenant_id(tenant_id);
            policy.setCreated_date(current_time);
            policy.setLast_updated_date(current_time);
            policy.setCreate_by(ref);
            policy.setLast_update_by(ref);
            return leavePolicyRepository.insert(policy);
        }
        return null;
    }

    public LeavePolicy getByTenant(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        return mongoTemplate.findOne(query, LeavePolicy.class);
    }

    public LeavePolicy update(CommandLeavePolicy command) throws Exception {
        if (StringUtils.isEmpty(command.getTenant_id())) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        LeavePolicy config = this.getByTenant(command.getTenant_id());
        if (config != null) {
            config.setLast_updated_date(System.currentTimeMillis());
            config.setOfficial_agent_total_leave(command.getOfficial_agent_total_leave() != null ? command.getOfficial_agent_total_leave() : config.getOfficial_agent_total_leave());
            config.setLeave_cut_off(command.getLeave_cut_off() != null ? command.getLeave_cut_off() : config.getLeave_cut_off());
            config.setLeave_type(StringUtils.isNotBlank(command.getLeave_type()) ? command.getLeave_type() : config.getLeave_type());
            config.setLast_update_by(command.getRef());
            return leavePolicyRepository.save(config);
        }
        return null;
    }
}
