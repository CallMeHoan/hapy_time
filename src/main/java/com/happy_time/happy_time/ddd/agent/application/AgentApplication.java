package com.happy_time.happy_time.ddd.agent.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.command.CommandChangePassword;
import com.happy_time.happy_time.ddd.agent.command.CommandSearchAgent;
import com.happy_time.happy_time.ddd.agent.command.CommandValidate;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.agent.model.AgentV0;
import com.happy_time.happy_time.ddd.agent.model.AgentView;
import com.happy_time.happy_time.ddd.agent.repository.IAgentRepository;
import com.happy_time.happy_time.ddd.auth.application.AuthApplication;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.department.Department;
import com.happy_time.happy_time.ddd.department.application.DepartmentApplication;
import com.happy_time.happy_time.ddd.jedis.JedisMaster;
import com.happy_time.happy_time.ddd.position.Position;
import com.happy_time.happy_time.ddd.position.application.PositionApplication;
import com.happy_time.happy_time.ddd.tenant.application.TenantApplication;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
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
import java.util.Map;

import static com.happy_time.happy_time.ddd.jedis.JedisMaster.COLON;

@Component
public class AgentApplication {
    @Autowired
    private IAgentRepository iAgentRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TenantApplication tenantApplication;
    @Autowired
    private DepartmentApplication departmentApplication;
    @Autowired
    private PositionApplication positionApplication;
    @Autowired
    private JedisMaster jedisMaster;
    public Page<Agent> search(CommandSearchAgent command, Integer page, Integer size) throws Exception {
        List<Agent> agents = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();

        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(command.getAgent_status() != null) {
            query.addCriteria(Criteria.where("agent_status").is(command.getAgent_status()));
        }
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getAgent_id())) {
            query.addCriteria(Criteria.where("agent_id").is(command.getAgent_id()));
        }
        if(StringUtils.isNotBlank(command.getName())) {
            query.addCriteria(Criteria.where("name").regex(HAPStringUtils.stripAccents(command.getName()),"i"));
        }
        if(StringUtils.isNotBlank(command.getPhone_number())) {
            query.addCriteria(Criteria.where("phone_number").is(command.getPhone_number()));
        }
        if(command.getRole() != null) {
            query.addCriteria(Criteria.where("role").is(command.getRole()));
        }
        if(command.getIs_used_happy_time() != null) {
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
        if(StringUtils.isNotBlank(command.getAgent_position())) {
            query.addCriteria(Criteria.where("position_id").is(command.getAgent_position()));
        }
        Long total = mongoTemplate.count(query, Agent.class);
        agents = mongoTemplate.find(query.with(pageRequest), Agent.class);
        return PageableExecutionUtils.getPage(
                agents,
                pageRequest,
                () -> total);
    }

    public Agent create(Agent agent) {
        Tenant tenant = tenantApplication.getById(new ObjectId(agent.getTenant_id()));
        if (tenant != null) {
            Query query = new Query();
            query.addCriteria(new Criteria("is_deleted").is(false));
            query.addCriteria(new Criteria("tenant_id").is(tenant.get_id().toHexString()));
            Long total = mongoTemplate.count(query, Agent.class) + 1;
            String agent_code = tenant.getCompany_shorthand() + total;
            agent.setAgent_code(agent_code);
        }
        Long current_time = System.currentTimeMillis();
        agent.setIs_deleted(false);
        agent.setCreated_date(current_time);
        agent.setLast_updated_date(current_time);
        iAgentRepository.save(agent);
        reloadCache(agent);
        return agent;
    }

    public Agent update(Agent agent, String id) {
        Long current_time = System.currentTimeMillis();
        Agent update = mongoTemplate.findById(id, Agent.class);
        if(update != null) {
            update.setLast_updated_date(current_time);
            update.setAvatar(StringUtils.isNotBlank(agent.getAvatar()) ? agent.getAvatar() : update.getAvatar());
            update.setName(StringUtils.isNotBlank(agent.getName()) ? agent.getName() : update.getName());
            update.setGender(agent.getGender() != null ? agent.getGender() : update.getGender());
            update.setPhone_number(StringUtils.isNotBlank(agent.getPhone_number()) ? agent.getPhone_number() : update.getPhone_number());
            update.setDate_of_birth(agent.getDate_of_birth() != null ? agent.getDate_of_birth() : update.getDate_of_birth());
            update.setPersonal_mail(StringUtils.isNotBlank(agent.getPersonal_mail()) ? agent.getPersonal_mail() : update.getPersonal_mail());
            update.setCompany_mail(StringUtils.isNotBlank(agent.getCompany_mail()) ? agent.getCompany_mail() : update.getCompany_mail());
            update.setIdentify_id(StringUtils.isNotBlank(agent.getIdentify_id()) ? agent.getIdentify_id() : update.getIdentify_id());
            update.setIssued_by(StringUtils.isNotBlank(agent.getIssued_by()) ? agent.getIssued_by() : update.getIssued_by());
            update.setIssued_date(agent.getIssued_date() != null ? agent.getIssued_date() : update.getIssued_date());
            update.setStaying_address(StringUtils.isNotBlank(agent.getStaying_address()) ? agent.getStaying_address() : update.getStaying_address());
            update.setResidence_address(StringUtils.isNotBlank(agent.getResidence_address()) ? agent.getResidence_address() : update.getResidence_address());
            update.setPersonal_tax_id(StringUtils.isNotBlank(agent.getPersonal_tax_id()) ? agent.getPersonal_tax_id() : update.getPersonal_tax_id());
            update.setEducation_type(agent.getEducation_type() != null ? agent.getEducation_type() : update.getEducation_type());
            update.setNote(StringUtils.isNotBlank(agent.getNote()) ? agent.getNote() : update.getNote());
            update.setSchool_name(StringUtils.isNotBlank(agent.getSchool_name()) ? agent.getSchool_name() : update.getSchool_name());
            update.setMajor(StringUtils.isNotBlank(agent.getMajor()) ? agent.getMajor() : update.getMajor());
            update.setGraduation_date(agent.getGraduation_date() != null ? agent.getGraduation_date() : update.getGraduation_date());
            update.setMarried_status(agent.getMarried_status() != null ? agent.getMarried_status() : update.getMarried_status());
            update.setBank_account_number(StringUtils.isNotBlank(agent.getBank_account_number()) ? agent.getBank_account_number() : update.getBank_account_number());
            update.setBank(StringUtils.isNotBlank(agent.getBank()) ? agent.getBank() : update.getBank());
            update.setBank_branch(StringUtils.isNotBlank(agent.getBank_branch()) ? agent.getBank_branch() : update.getBank_branch());
            update.setPosition_id(StringUtils.isNotBlank(agent.getPosition_id()) ? agent.getPosition_id() : update.getPosition_id());
            update.setDepartment_id(StringUtils.isNotBlank(agent.getDepartment_id()) ? agent.getDepartment_id() : update.getDepartment_id());
            update.setDevice_id(StringUtils.isNotBlank(agent.getDevice_id()) ? agent.getDevice_id() : update.getDevice_id());
            update.setStart_working_date(agent.getStart_working_date() != null ? agent.getStart_working_date() : update.getStart_working_date());
            update.setAgent_status(agent.getAgent_status() != null ? agent.getAgent_status() : update.getAgent_status());
            update.setAgent_type(agent.getAgent_type() != null ? agent.getAgent_type() : update.getAgent_type());
            update.setTotal_date_off(agent.getTotal_date_off() != null ? agent.getTotal_date_off() : update.getTotal_date_off());
            update.setIs_used_happy_time(agent.getIs_used_happy_time() != null ? agent.getIs_used_happy_time() : update.getIs_used_happy_time());
            update.setStop_working_date(agent.getStop_working_date() != null ? agent.getStop_working_date() : update.getStop_working_date());
            update.setDevice_id(StringUtils.isNotBlank(agent.getDevice_id()) ? agent.getDevice_id() : update.getDevice_id());
            update.setIs_has_account(agent.getIs_has_account() != null ? agent.getIs_has_account() : update.getIs_has_account());
            update.setLast_login_info(agent.getLast_login_info() != null ? agent.getLast_login_info() : update.getLast_login_info());
            update.setDevice_id(StringUtils.isNotBlank(agent.getDevice_id()) ? agent.getDevice_id() : update.getDevice_id());
            Agent res = mongoTemplate.save(update, "agents");
            reloadCache(res);
            return res;
        }
        else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        Agent agent = mongoTemplate.findById(id, Agent.class);
        if(agent != null) {
            agent.setIs_deleted(true);
            agent.setLast_updated_date(current_time);
            agent.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            agent.getLast_update_by().setUpdated_at(System.currentTimeMillis());

            //xóa cache
            deleteCache(agent.get_id().toHexString(), agent.getTenant_id());

            //Xóa mongo
            mongoTemplate.save(agent, "agents");
            return true;
        } else return false;
    }

    public Agent getById(ObjectId id) {
        Agent agent = mongoTemplate.findById(id, Agent.class);
        if(agent != null) {
            if (agent.getIs_deleted()) return null;
            if (StringUtils.isNotBlank(agent.getPosition_id())) {
                Position position = positionApplication.getById(agent.getPosition_id());
                if (position != null) {
                    agent.setPosition_name(position.getPosition_name());
                }
            }
            if (StringUtils.isNotBlank(agent.getDepartment_id())) {
                Department department = departmentApplication.getById(agent.getDepartment_id());
                if (department != null) {
                    agent.setDepartment_name(department.getDepartment_name());
                }
            }
            return agent;
        } else return null;
    }

    public List<AgentV0> setViewAgent(List<Agent> agents) {
        List<AgentV0> list = new ArrayList<>();
        Tenant tenant = tenantApplication.getById(new ObjectId(agents.get(0).getTenant_id()));
        for (Agent agent: agents) {
            String department_name = "";
            String position_name = "";

            //get name of department
            if (StringUtils.isNotBlank(agent.getDepartment_id())) {
                Department department = departmentApplication.getById(agent.getDepartment_id());
                if (department != null) {
                    department_name = department.getDepartment_name();
                }
            }

            //get name of position
            if (StringUtils.isNotBlank(agent.getPosition_id())) {
                Position position = positionApplication.getById(agent.getPosition_id());
                if (position != null) {
                    position_name = position.getPosition_name();
                }
            }
            AgentV0 agentV0 = AgentV0.builder()
                    ._id(agent.get_id().toHexString())
                    .name(agent.getName())
                    .avatar(agent.getAvatar())
                    .personal_mail(agent.getPersonal_mail())
                    .role(agent.getRole())
                    .is_used_happy_time(agent.getIs_used_happy_time())
                    .agent_status(agent.getAgent_status())
                    .phone_number(agent.getPhone_number())
                    .agent_code(agent.getAgent_code())
                    .department_name(department_name)
                    .position_name(position_name)
                    .last_login_info(agent.getLast_login_info())
                    .agent_type(agent.getAgent_type())
                    .device_id(agent.getDevice_id())
                    .company_name(tenant.getCompany_name())
                    .gender(agent.getGender())
                    .start_working_date(agent.getStart_working_date())
                    .build();
            list.add(agentV0);
        }
        return list;
    }

    public Boolean validatePhoneNumberAndEmail(CommandValidate command) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (StringUtils.isNotBlank(command.getPhone_number())) {
            query.addCriteria(Criteria.where("phone_number").is(command.getPhone_number()));
        }
        if (StringUtils.isNotBlank(command.getPersonal_mail())) {
            query.addCriteria(Criteria.where("personal_mail").is(command.getPersonal_mail()));
        }

        return mongoTemplate.exists(query, Agent.class);
    }

    public Boolean changePassword(CommandChangePassword command) throws Exception {
        if (StringUtils.isBlank(command.getNew_password()) || StringUtils.isBlank(command.getOld_password())
                || StringUtils.isBlank(command.getAgent_id()) || StringUtils.isBlank(command.getTenant_id())
                || StringUtils.isBlank(command.getPhone_number())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("agent_id").is(command.getAgent_id()));
        query.addCriteria(Criteria.where("phone_number").is(command.getPhone_number()));
        Account account = mongoTemplate.findOne(query, Account.class);
        if (account == null) {
            throw new Exception(ExceptionMessage.ACCOUNT_NOT_EXIST);
        }
        if (!account.getPassword().equals(command.getOld_password())) {
            throw new Exception(ExceptionMessage.INCORRECT_PASSWORD);
        }
        account.setPassword(command.getNew_password());
        account.setLast_updated_date(System.currentTimeMillis());
        Account updated = mongoTemplate.save(account, "accounts");
        return true;
    }

    public List<Agent> getByTenant(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
//        query.addCriteria(Criteria.where("agent_status").is(2));
        return mongoTemplate.find(query, Agent.class);
    }

    public List<Agent> getByIds(List<String> ids) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("_id").in(ids));
        return mongoTemplate.find(query, Agent.class);
    }

    public List<Agent> getByPositionIds(List<String> ids) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("position_id").in(ids));
        return mongoTemplate.find(query, Agent.class);
    }

    public List<Agent> getByDepartmentIds(List<String> ids) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("department_id").in(ids));
        return mongoTemplate.find(query, Agent.class);
    }

    public Agent getByPhoneNumber(String phone_number) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("phone_number").is(phone_number));
        return mongoTemplate.findOne(query, Agent.class);
    }

    public AgentView setView(String agent_id, String tenant_id){
        //check trên redis có không nếu có thì sẽ lấy trên redis (cải thiện tốc độ setview)
        String key = JedisMaster.JedisPrefixKey.agent_tenant + COLON + tenant_id  + COLON + agent_id;
        Map<String, String> agent_redis = jedisMaster.hgetAll(key);
        if (agent_redis != null) {
            return AgentView.builder()
                    .id(agent_id)
                    .name(agent_redis.get("name"))
                    .position(agent_redis.get("position"))
                    .avatar(agent_redis.get("avatar"))
                    .build();
        } else {
            Query query = new Query();
            query.addCriteria(Criteria.where("is_deleted").is(false));
            query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
            query.addCriteria(Criteria.where("_id").is(agent_id));
            Agent agent = mongoTemplate.findOne(query, Agent.class);
            if (agent != null) {
                String position_name = null;
                if (StringUtils.isNotBlank(agent.getPosition_id())) {
                    Position position = positionApplication.getById(agent.getPosition_id());
                    if (position != null) {
                        position_name = position.getPosition_name();
                    }
                }

                return AgentView.builder()
                        .id(agent_id)
                        .name(agent.getName())
                        .position(position_name)
                        .avatar(agent.getAvatar())
                        .build();
            }
        }
        return null;
    }

    private void reloadCache(Agent agent) {
        if (agent != null) {
            String key = JedisMaster.JedisPrefixKey.agent_tenant + COLON + agent.getTenant_id()  + COLON + agent.get_id();
            Map<String, String> agent_redis = jedisMaster.hgetAll(key);
            // lấy position hiện tại để set
            String position_name = "";
            if (StringUtils.isNotBlank(agent.getPosition_id())) {
                Position position = positionApplication.getById(agent.getPosition_id());
                if (position != null) {
                    position_name = position.getPosition_name();
                }
            }
            agent_redis.put("name", StringUtils.isNotBlank(agent.getName()) ? agent.getName() : "");
            agent_redis.put("avatar", StringUtils.isNotBlank(agent.getAvatar()) ? agent.getAvatar() : "");
            agent_redis.put("position", position_name);
            jedisMaster.hSetAll(key, agent_redis);
        }
    }

    private void deleteCache(String agent_id, String tenant_id) {
        String key = JedisMaster.JedisPrefixKey.agent_tenant + COLON + tenant_id + COLON + agent_id;
        jedisMaster.hDel(key);
    }
}
