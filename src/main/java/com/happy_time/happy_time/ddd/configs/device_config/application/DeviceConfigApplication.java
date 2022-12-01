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
        if(StringUtils.isNotBlank(command.getIp_name())) {
            query.addCriteria(Criteria.where("name").regex(command.getIp_name(),"i"));
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

    public DeviceConfig create(DeviceConfig ipConfig) {
        Long current = System.currentTimeMillis();
        ipConfig.setCreated_date(current);
        ipConfig.setLast_updated_date(current);
        iIPConfigRepository.save(ipConfig);
        return ipConfig;
    }

    public DeviceConfig update(DeviceConfig ipConfig) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(ipConfig.get_id()));
        query.addCriteria(Criteria.where("tenant_id").is(ipConfig.getTenant_id()));
        Boolean is_exists = mongoTemplate.exists(query, DeviceConfig.class);
        if(is_exists) {
            ipConfig.setLast_updated_date(current_time);
            return mongoTemplate.save(ipConfig, "device_config");
        }
        else return null;
    }

    public DeviceConfig getById(ObjectId id) {
        DeviceConfig ipConfig = mongoTemplate.findById(id, DeviceConfig.class);
        if(ipConfig != null) {
            if (ipConfig.getIs_deleted()) return null;
            return ipConfig;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        DeviceConfig ipConfig = mongoTemplate.findById(id, DeviceConfig.class);
        if(ipConfig != null) {
            ipConfig.setIs_deleted(true);
            ipConfig.setLast_updated_date(current_time);
            ipConfig.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            ipConfig.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(ipConfig, "device_config");
            return true;
        } else return false;
    }
}
