package com.happy_time.happy_time.ddd.gps_config.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
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
public class GPSConfigApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IGPSConfigRepository igpsConfigRepository;

    public Page<GPSConfig> search(CommandGPSConfig command, Integer page, Integer size) throws Exception {
        List<GPSConfig> configs = new ArrayList<>();
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
            query.addCriteria(Criteria.where("gps_name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
        }

        configs = mongoTemplate.find(query.with(pageRequest), GPSConfig.class);
        return PageableExecutionUtils.getPage(
                configs,
                pageRequest,
                () -> mongoTemplate.count(query, BSSIDConfig.class));
    }

    public GPSConfig create(GPSConfig config) throws Exception {
        if (StringUtils.isBlank(config.getGps_name())
                || StringUtils.isBlank(config.getAddress())
                || config.getLat() == null
                || config.getLon() == null
                || config.getRadius() == null) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String name_unsigned = HAPStringUtils.stripAccents(config.getGps_name()).toLowerCase(Locale.ROOT);
        String address_unsigned = HAPStringUtils.stripAccents(config.getAddress()).toLowerCase(Locale.ROOT);
        config.setGps_name_unsigned(address_unsigned);
        config.setAddress_unsigned(name_unsigned);
        Long current = System.currentTimeMillis();
        config.setCreated_date(current);
        config.setLast_updated_date(current);
        igpsConfigRepository.insert(config);
        return config;
    }

    public GPSConfig update(CommandGPSConfig command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        GPSConfig config = mongoTemplate.findOne(query, GPSConfig.class);
        if(config != null) {
            if (StringUtils.isBlank(command.getGps_name())
                    || StringUtils.isBlank(command.getAddress())
                    || command.getLat() == null
                    || command.getLon() == null
                    || command.getRadius() == null) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            String name_unsigned = HAPStringUtils.stripAccents(command.getGps_name()).toLowerCase(Locale.ROOT);
            String address_unsigned = HAPStringUtils.stripAccents(command.getAddress()).toLowerCase(Locale.ROOT);
            config.setGps_name(command.getGps_name());
            config.setAddress(command.getAddress());
            config.setAddress_unsigned(address_unsigned);
            config.setGps_name_unsigned(name_unsigned);
            config.setLat(command.getLat());
            config.setLon(command.getLon());
            config.setRadius(command.getRadius());
            config.setLast_updated_date(current_time);
            return mongoTemplate.save(config, "gps_config");
        }
        else return null;
    }

    public GPSConfig getById(ObjectId id) {
        GPSConfig config = mongoTemplate.findById(id, GPSConfig.class);
        if(config != null) {
            if (config.getIs_deleted()) return null;
            return config;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        GPSConfig config = mongoTemplate.findById(id, GPSConfig.class);
        if(config != null) {
            config.setIs_deleted(true);
            config.setLast_updated_date(current_time);
            config.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            config.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(config, "gps_config");
            return true;
        } else return false;
    }

    public List<GPSConfig> getByTenant(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        return mongoTemplate.find(query, GPSConfig.class);
    }
}
