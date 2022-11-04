package com.happy_time.happy_time.ddd.agent.application;

import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.command.CommandSearchAgent;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.agent.repository.IAgentRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentApplication {
    @Autowired
    private IAgentRepository iAgentRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    public Page<Agent> search(CommandSearchAgent command, Integer page, Integer size) throws Exception {
        List<Agent> agents = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();

        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getAgent_status())) {
            query.addCriteria(Criteria.where("agent_status").is(command.getAgent_status()));
        }
        if(StringUtils.isNotBlank(command.getUser_name())) {
            query.addCriteria(Criteria.where("user_name").regex(command.getUser_name()));
        }
        if(StringUtils.isNotBlank(command.getPhone_number())) {
            query.addCriteria(Criteria.where("phone_number").is(command.getPhone_number()));
        }
        if(StringUtils.isNotBlank(command.getRole())) {
            query.addCriteria(Criteria.where("role").is(command.getRole()));
        }
        if(StringUtils.isNotBlank(command.getAgent_position())) {
            query.addCriteria(Criteria.where("agent_position").is(command.getAgent_position()));
        }
        if(BooleanUtils.isTrue(command.getIs_used_happy_time())) {
            query.addCriteria(Criteria.where("is_used_happy_time").is(command.getIs_used_happy_time()));
        }
        if(StringUtils.isNotBlank(command.getPersonal_mail())) {
            query.addCriteria(Criteria.where("personal_mail").regex(command.getPersonal_mail()));
        }
        if(StringUtils.isNotBlank(command.getCompany_mail())) {
            query.addCriteria(Criteria.where("company_mail").regex(command.getCompany_mail()));
        }
        if(StringUtils.isNotBlank(command.getAgent_code())) {
            query.addCriteria(Criteria.where("agent_code").is(command.getAgent_code()));
        }
        if(command.getStart_working_date() != null) {
            query.addCriteria(Criteria.where("start_working_date").lte(command.getStart_working_date()).gte(command.getStart_working_date()));
        }
        if(command.getStart_working_date() != null) {
            query.addCriteria(Criteria.where("start_working_date").lte(command.getStart_working_date()).gte(command.getStart_working_date()));
        }
        if(command.getStop_working_date() != null) {
            query.addCriteria(Criteria.where("stop_working_date").lte(command.getStop_working_date()).gte(command.getStop_working_date()));
        }
        if(command.getOfficial_working_date() != null) {
            query.addCriteria(Criteria.where("official_working_date").lte(command.getOfficial_working_date()).gte(command.getOfficial_working_date()));
        }
        agents = mongoTemplate.find(query, Agent.class);

        return PageableExecutionUtils.getPage(
                agents,
                pageRequest,
                () -> mongoTemplate.count(query, Agent.class));
    }

    public Agent create (Agent agent) {
        Long current_time = System.currentTimeMillis();
        agent.setIs_deleted(false);
        agent.setCreated_date(current_time);
        agent.setLast_updated_date(current_time);
        iAgentRepository.save(agent);
        return agent;
    }

    public Boolean edit (Agent agent) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(agent.get_id()));
        Boolean is_exists = mongoTemplate.exists(query, Agent.class);
        if(is_exists) {
            agent.setLast_updated_date(current_time);
            iAgentRepository.save(agent);
            return true;
        }
        else return false;
    }

    public Boolean delete (ObjectId id) {
        Long current_time = System.currentTimeMillis();
        Agent agent = mongoTemplate.findById(id, Agent.class);
        if(agent != null) {
            agent.setIs_deleted(true);
            agent.setLast_updated_date(current_time);
            iAgentRepository.save(agent);
            return true;
        } else return false;
    }

    public Agent getById (ObjectId id) {
        Agent agent = mongoTemplate.findById(id, Agent.class);
        if(agent != null) {
            if (agent.getIs_deleted()) return null;
            return agent;
        } else return null;
    }
}
