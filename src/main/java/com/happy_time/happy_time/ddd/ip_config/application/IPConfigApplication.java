package com.happy_time.happy_time.ddd.ip_config.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.ip_config.command.CommandIPConfig;
import com.happy_time.happy_time.ddd.ip_config.repository.IIpConfigRepository;
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
        if(StringUtils.isNotBlank(command.getKeyword())) {
            query.addCriteria(Criteria.where("ip_name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
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

    public IPConfig create(IPConfig ipConfig) throws Exception {
        if (StringUtils.isBlank(ipConfig.getIp_name()) || StringUtils.isBlank(ipConfig.getIp_address())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String name_unsigned = HAPStringUtils.stripAccents(ipConfig.getIp_name()).toLowerCase(Locale.ROOT);
        ipConfig.setIp_name_unsigned(name_unsigned);
        Long current = System.currentTimeMillis();
        ipConfig.setCreated_date(current);
        ipConfig.setLast_updated_date(current);
        iIPConfigRepository.insert(ipConfig);
        return ipConfig;
    }

    public IPConfig update(CommandIPConfig command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        IPConfig config = mongoTemplate.findOne(query, IPConfig.class);
        if(config != null) {
            if (StringUtils.isBlank(command.getIp_address()) || StringUtils.isBlank(command.getIp_name())) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            String name_unsigned = HAPStringUtils.stripAccents(command.getIp_name()).toLowerCase(Locale.ROOT);
            config.setLast_updated_date(current_time);
            config.setIp_name_unsigned(name_unsigned);
            config.setIp_name(command.getIp_name());
            config.setIp_address(command.getIp_address());
            config.setLast_update_by(command.getLast_updated_by());
            return mongoTemplate.save(config, "ip_config");
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