package com.happy_time.happy_time.ddd.configs.device_config.application;

import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.configs.device_config.command.CommandDeviceConfig;
import com.happy_time.happy_time.ddd.configs.device_config.model.DeviceConfig;
import com.happy_time.happy_time.ddd.configs.device_config.repository.IDeviceConfigRepository;
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
public class DeviceConfigApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IDeviceConfigRepository iIPConfigRepository;

    public Page<DeviceConfig> search(CommandDeviceConfig command, Integer page, Integer size) throws Exception {
        List<DeviceConfig> deviceConfigs = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getDevice_id())) {
            query.addCriteria(Criteria.where("device_id").is(command.getDevice_id()));
        }
        if(StringUtils.isNotBlank(command.getAgent_code())) {
            query.addCriteria(Criteria.where("agent_code").regex(command.getAgent_code(),"i"));
        }
        if(StringUtils.isNotBlank(command.getAgent_code())) {
            query.addCriteria(Criteria.where("agent_code").regex(command.getAgent_code(),"i"));
        }
        if(StringUtils.isNotBlank(command.getDepartment())) {
            query.addCriteria(Criteria.where("department").is(command.getDepartment()));
        }
        if(StringUtils.isNotBlank(command.getStatus())) {
            query.addCriteria(Criteria.where("status.name").is(command.getStatus()));
        }

        deviceConfigs = mongoTemplate.find(query, DeviceConfig.class);
        return PageableExecutionUtils.getPage(
                deviceConfigs,
                pageRequest,
                () -> mongoTemplate.count(query, DeviceConfig.class));
    }

    public DeviceConfig create(DeviceConfig deviceConfig) {
        Long current = System.currentTimeMillis();
        deviceConfig.setCreated_date(current);
        deviceConfig.setLast_updated_date(current);
        iIPConfigRepository.save(deviceConfig);
        return deviceConfig;
    }

    public DeviceConfig update(DeviceConfig deviceConfig) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(deviceConfig.get_id()));
        query.addCriteria(Criteria.where("tenant_id").is(deviceConfig.getTenant_id()));
        Boolean is_exists = mongoTemplate.exists(query, DeviceConfig.class);
        if(is_exists) {
            deviceConfig.setLast_updated_date(current_time);
            return mongoTemplate.save(deviceConfig, "device_config");
        }
        else return null;
    }

    public DeviceConfig getById(ObjectId id) {
        DeviceConfig deviceConfig = mongoTemplate.findById(id, DeviceConfig.class);
        if(deviceConfig != null) {
            if (deviceConfig.getIs_deleted()) return null;
            return deviceConfig;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        DeviceConfig deviceConfig = mongoTemplate.findById(id, DeviceConfig.class);
        if(deviceConfig != null) {
            deviceConfig.setIs_deleted(true);
            deviceConfig.setLast_updated_date(current_time);
            deviceConfig.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            deviceConfig.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(deviceConfig, "device_config");
            return true;
        } else return false;
    }
}
