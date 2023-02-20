package com.happy_time.happy_time.ddd.request_procedure.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.gps_config.GPSConfig;
import com.happy_time.happy_time.ddd.gps_config.command.CommandGPSConfig;
import com.happy_time.happy_time.ddd.request_procedure.RequestProcedure;
import com.happy_time.happy_time.ddd.request_procedure.command.CommandRequestProcedure;
import com.happy_time.happy_time.ddd.request_procedure.repository.IRequestProcedureRepository;
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
public class RequestProcedureApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IRequestProcedureRepository iRequestProcedureRepository;

    public Page<RequestProcedure> search(CommandRequestProcedure command, Integer page, Integer size) throws Exception {
        List<RequestProcedure> configs = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getType())) {
            query.addCriteria(Criteria.where("type").is(command.getType()));
        } else {
            query.addCriteria(Criteria.where("type").is("company"));
        }
        if(StringUtils.isNotBlank(command.getKeyword())) {
            query.addCriteria(Criteria.where("name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
        }

        configs = mongoTemplate.find(query, RequestProcedure.class);
        return PageableExecutionUtils.getPage(
                configs,
                pageRequest,
                () -> mongoTemplate.count(query, BSSIDConfig.class));
    }

    public RequestProcedure create(RequestProcedure config) throws Exception {
        if (StringUtils.isBlank(config.getName())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String name_unsigned = HAPStringUtils.stripAccents(config.getName()).toLowerCase(Locale.ROOT);
        config.setName_unsigned(name_unsigned);
        Long current = System.currentTimeMillis();
        config.setCreated_date(current);
        config.setLast_updated_date(current);
        iRequestProcedureRepository.insert(config);
        return config;
    }

    public RequestProcedure update(CommandRequestProcedure command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        RequestProcedure config = mongoTemplate.findOne(query, RequestProcedure.class);
        if(config != null) {
            if (StringUtils.isBlank(command.getName())) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            String name_unsigned = HAPStringUtils.stripAccents(command.getName()).toLowerCase(Locale.ROOT);
            config.setName_unsigned(command.getName());
            config.setName_unsigned(name_unsigned);
            config.setDepartments(command.getDepartments());
            config.setPositions(command.getPositions());
            config.setAgents(command.getAgents());
            config.setStages(command.getStages());
            config.setRequest_ids(command.getRequest_ids());
            config.setFollows(command.getFollows());
            config.setType(command.getType());
            config.setLast_update_by(command.getLast_updated_by());
            config.setLast_updated_date(current_time);
            return mongoTemplate.save(config, "request_procedure");
        }
        else return null;
    }

    public RequestProcedure getById(ObjectId id) {
        RequestProcedure config = mongoTemplate.findById(id, RequestProcedure.class);
        if(config != null) {
            if (config.getIs_deleted()) return null;
            return config;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        RequestProcedure config = mongoTemplate.findById(id, RequestProcedure.class);
        if(config != null) {
            config.setIs_deleted(true);
            config.setLast_updated_date(current_time);
            config.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            config.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(config, "request_procedure");
            return true;
        } else return false;
    }
}
