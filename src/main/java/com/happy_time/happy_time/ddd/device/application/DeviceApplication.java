package com.happy_time.happy_time.ddd.device.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.agent.model.AgentView;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.device.Device;
import com.happy_time.happy_time.ddd.device.command.CommandDevice;
import com.happy_time.happy_time.ddd.device.repository.IDeviceRepository;
import com.happy_time.happy_time.ddd.gps_config.GPSConfig;
import com.happy_time.happy_time.ddd.gps_config.command.CommandGPSConfig;
import com.happy_time.happy_time.ddd.gps_config.repository.IGPSConfigRepository;
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
import java.util.Locale;

@Component
public class DeviceApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IDeviceRepository iDeviceRepository;

    @Autowired
    private AgentApplication agentApplication;

    public Page<Device> search(CommandDevice command, Integer page, Integer size) throws Exception {
        List<Device> configs = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }

        configs = mongoTemplate.find(query, Device.class);
        return PageableExecutionUtils.getPage(
                configs,
                pageRequest,
                () -> mongoTemplate.count(query, Device.class));
    }

    private Query queryBuilder(CommandDevice command) throws Exception {
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if (StringUtils.isNotBlank(command.getAgent_id())) {
            query.addCriteria(Criteria.where("agent_id").is(command.getAgent_id()));
        }
        return query;
    }

    public Device create(Device device) throws Exception {
        if (StringUtils.isBlank(device.getDevice_id())
                || StringUtils.isBlank(device.getDevice_name())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Agent agent = agentApplication.getById(new ObjectId(device.getAgent_id()));
        if (agent == null) {
            throw new Exception(ExceptionMessage.AGENT_NOT_EXIST);
        }
        //set lần cuối đăng nhập của agent
        Agent.LastLoginInfo info = Agent.LastLoginInfo.builder()
                .device_name(device.getDevice_name())
                .time(System.currentTimeMillis())
                .build();
        agent.setLast_login_info(info);
        AgentView view = AgentView.builder()
                .id(device.getAgent_id())
                .name(agent.getName())
                .avatar(agent.getAvatar())
                .position(agent.getPosition_name())
                .build();
        device.setAgent_view(view);
        //check xem đã có device nào của agent này hay chưa
        CommandDevice command = CommandDevice.builder()
                .tenant_id(device.getTenant_id())
                .agent_id(device.getAgent_id())
                .build();
        Query query = this.queryBuilder(command);
        long total = mongoTemplate.count(query, Device.class);
        if (total > 0) {
            device.setStatus(false);
        } else {
            //nếu chưa có thì sẽ set device này là device chấm công
            agent.setDevice_id(device.getDevice_id());
        }
        Long current = System.currentTimeMillis();
        device.setCreated_date(current);
        device.setLast_updated_date(current);
        iDeviceRepository.insert(device);
        agentApplication.update(agent,device.getAgent_id());
        return device;
    }

    public Device update(CommandDevice command, String id) throws Exception {
        Agent agent = agentApplication.getById(new ObjectId(command.getAgent_id()));
        if (agent == null) {
            throw new Exception(ExceptionMessage.AGENT_NOT_EXIST);
        }
        AgentView view = AgentView.builder()
                .id(command.getAgent_id())
                .name(agent.getName())
                .avatar(agent.getAvatar())
                .position(agent.getPosition_name())
                .build();
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        Device device = mongoTemplate.findOne(query, Device.class);
        if(device != null) {
            device.setLast_updated_date(current_time);
            device.setStatus(command.getStatus() != null ? command.getStatus() : device.getStatus());
            device.setDevice_id(StringUtils.isNotBlank(command.getDevice_id()) ? command.getDevice_id() : device.getDevice_id());
            device.setDevice_name(StringUtils.isNotBlank(command.getDevice_name()) ? command.getDevice_name() : device.getDevice_name());
            device.setAgent_view(view);
            return mongoTemplate.save(device, "devices");
        }
        else return null;
    }

    public Device getById(ObjectId id) {
        Device device = mongoTemplate.findById(id, Device.class);
        if(device != null) {
            if (device.getIs_deleted()) return null;
            return device;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        Device device = mongoTemplate.findById(id, Device.class);
        if(device != null) {
            device.setIs_deleted(true);
            device.setLast_updated_date(current_time);
            device.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            device.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(device, "devices");
            return true;
        } else return false;
    }

    public List<Device> getByTenant(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        return mongoTemplate.find(query, Device.class);
    }
}
