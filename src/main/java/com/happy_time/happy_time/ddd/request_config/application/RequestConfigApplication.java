package com.happy_time.happy_time.ddd.request_config.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.request_config.RequestConfig;
import com.happy_time.happy_time.ddd.request_config.command.CommandRequestConfig;
import com.happy_time.happy_time.ddd.request_config.repository.IRequestConfigRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class RequestConfigApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IRequestConfigRepository iRequestConfigRepository;

    public RequestConfig create(RequestConfig config) throws Exception {
        if (StringUtils.isEmpty(config.getRequest_name()) || StringUtils.isEmpty(config.getDescription())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Long current_time = System.currentTimeMillis();
        String name_unsigned = HAPStringUtils.stripAccents(config.getRequest_name()).toLowerCase(Locale.ROOT);
        config.setRequest_name_unsigned(name_unsigned);
        config.setCreated_date(current_time);
        config.setLast_updated_date(current_time);
        return iRequestConfigRepository.insert(config);
    }

    public List<RequestConfig> clone(ReferenceData ref, String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(AppConstant.TENANT_DEFAULT));
        List<RequestConfig> configs = mongoTemplate.find(query, RequestConfig.class);
        Long current_time = System.currentTimeMillis();
        for (RequestConfig config : configs) {
            config.setTenant_id(tenant_id);
            config.setCreated_date(current_time);
            config.setLast_updated_date(current_time);
            config.setCreate_by(ref);
            config.setLast_update_by(ref);
        }
        return iRequestConfigRepository.insert(configs);
    }

    public RequestConfig update(CommandRequestConfig command, String id) throws Exception {
        if (StringUtils.isEmpty(command.getTenant_id())) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        RequestConfig config = this.getById(id);
        if (config != null) {
            config.setLast_updated_date(System.currentTimeMillis());
            config.setProcedure(!StringUtils.isEmpty(command.getProcedure()) ? command.getProcedure() : "");
            config.setIs_in_use(command.getIs_in_use() != null ? command.getIs_in_use() : config.getIs_in_use());
            config.setLast_update_by(command.getRef());
            RequestConfig res = iRequestConfigRepository.save(config);
            return res;
        }
        return config;
    }

    public RequestConfig getById(String id) {
        return iRequestConfigRepository.findById(id).orElse(null);
    }

    public List<RequestConfig> getAllByTenant(String tenant_id) throws Exception {
        if (StringUtils.isEmpty(tenant_id)) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        return mongoTemplate.find(query, RequestConfig.class);
    }
}
