package com.happy_time.happy_time.ddd.bssid_config.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.bssid_config.command.CommandBssidConfig;
import com.happy_time.happy_time.ddd.bssid_config.repository.IBssidConfigRepository;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
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
public class BssidConfigApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IBssidConfigRepository iBssidConfigRepository;

    public Page<BSSIDConfig> search(CommandBssidConfig command, Integer page, Integer size) throws Exception {
        List<BSSIDConfig> bssidConfigs = new ArrayList<>();
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
            query.addCriteria(Criteria.where("bssid_name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
        }

        bssidConfigs = mongoTemplate.find(query.with(pageRequest), BSSIDConfig.class);
        return PageableExecutionUtils.getPage(
                bssidConfigs,
                pageRequest,
                () -> mongoTemplate.count(query, BSSIDConfig.class));
    }

    public BSSIDConfig create(BSSIDConfig bssidConfig) throws Exception {
        if (StringUtils.isBlank(bssidConfig.getBssid_name()) || StringUtils.isBlank(bssidConfig.getBssid_address())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String name_unsigned = HAPStringUtils.stripAccents(bssidConfig.getBssid_name()).toLowerCase(Locale.ROOT);
        bssidConfig.setBssid_name_unsigned(name_unsigned);
        Long current = System.currentTimeMillis();
        bssidConfig.setCreated_date(current);
        bssidConfig.setLast_updated_date(current);
        iBssidConfigRepository.insert(bssidConfig);
        return bssidConfig;
    }

    public BSSIDConfig update(CommandBssidConfig command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        BSSIDConfig bssidConfig = mongoTemplate.findOne(query, BSSIDConfig.class);
        if(bssidConfig != null) {
            if (StringUtils.isBlank(command.getBssid_name()) || StringUtils.isBlank(command.getBssid_address())) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            String name_unsigned = HAPStringUtils.stripAccents(command.getBssid_name()).toLowerCase(Locale.ROOT);
            bssidConfig.setBssid_name_unsigned(name_unsigned);
            bssidConfig.setBssid_name(command.getBssid_name());
            bssidConfig.setBssid_address(command.getBssid_address());
            bssidConfig.setLast_updated_date(current_time);
            return mongoTemplate.save(bssidConfig, "bssid_config");
        }
        else return null;
    }

    public BSSIDConfig getById(ObjectId id) {
        BSSIDConfig bssidConfig = mongoTemplate.findById(id, BSSIDConfig.class);
        if(bssidConfig != null) {
            if (bssidConfig.getIs_deleted()) return null;
            return bssidConfig;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        BSSIDConfig bssidConfig = mongoTemplate.findById(id, BSSIDConfig.class);
        if(bssidConfig != null) {
            bssidConfig.setIs_deleted(true);
            bssidConfig.setLast_updated_date(current_time);
            bssidConfig.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            bssidConfig.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(bssidConfig, "bssid_config");
            return true;
        } else return false;
    }

    public List<BSSIDConfig> getByTenant(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        return mongoTemplate.find(query, BSSIDConfig.class);
    }
}
