package com.happy_time.happy_time.ddd.configuration.ip_config.application;

import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.configuration.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.configuration.ip_config.command.CommandIPConfig;
import com.happy_time.happy_time.ddd.configuration.ip_config.repository.IIpConfigRepository;
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
public class IPConfigApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IIpConfigRepository iIPConfigRepository;

    public Page<IPConfig> search(CommandIPConfig command, Integer page, Integer size) throws Exception {
        List<IPConfig> ipConfigList = new ArrayList<>();
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

        ipConfigList = mongoTemplate.find(query, IPConfig.class);
        return PageableExecutionUtils.getPage(
                ipConfigList,
                pageRequest,
                () -> mongoTemplate.count(query, IPConfig.class));
    }

    public IPConfig create(IPConfig ipConfig) {
        Long current = System.currentTimeMillis();
        ipConfig.setCreated_date(current);
        ipConfig.setLast_updated_date(current);
        iIPConfigRepository.save(ipConfig);
        return ipConfig;
    }

    public IPConfig update(IPConfig ipConfig, String id) {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(ipConfig.getTenant_id()));
        Boolean is_exists = mongoTemplate.exists(query, IPConfig.class);
        if(is_exists) {
            ipConfig.setLast_updated_date(current_time);
            return mongoTemplate.save(ipConfig, "ip_config");
        }
        else return null;
    }

    public IPConfig getById(ObjectId id) {
        IPConfig ipConfig = mongoTemplate.findById(id, IPConfig.class);
        if(ipConfig != null) {
            if (ipConfig.getIs_deleted()) return null;
            return ipConfig;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        IPConfig ipConfig = mongoTemplate.findById(id, IPConfig.class);
        if(ipConfig != null) {
            ipConfig.setIs_deleted(true);
            ipConfig.setLast_updated_date(current_time);
            ipConfig.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            ipConfig.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(ipConfig, "ip_config");
            return true;
        } else return false;
    }
}