package com.happy_time.happy_time.ddd.tenant.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.tenant.application.TenantApplication;
import com.happy_time.happy_time.ddd.tenant.model.Tenant;
import com.happy_time.happy_time.ddd.tenant.repository.ITenantRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/tenant")
public class TenantController {
    @Autowired
    private TenantApplication tenantApplication;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private ITenantRepository iTenantRepository;

//    @PostMapping("/create")
//    public Optional<ResponseObject> create(@RequestBody CommandCreateTenant){
//        Boolean created = tenantApplication.create(tenant, agent);
//        if(created) {
//            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("create_agent_successfully").build();
//            return Optional.of(res);
//        } else {
//            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("create_agent_failed").build();
//            return Optional.of(res);
//        }
//    }

    @GetMapping("/get")
    public Optional<ResponseObject> getById(HttpServletRequest httpServletRequest) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            Tenant tenant = tenantApplication.getById(new ObjectId(tenant_id));
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(tenant).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PutMapping("/update")
    public Optional<ResponseObject> edit(HttpServletRequest httpServletRequest, @RequestBody Tenant tenant) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.UPDATE_ACTION)
                    .build();
            tenant.setLast_updated_by(ref);
            tenant.set_id(new ObjectId(tenant_id));
            Tenant edited = tenantApplication.edit(tenant);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(edited).build();
            return Optional.of(res);
        }
        catch (Exception e){
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PutMapping("/change_status")
    public Optional<ResponseObject> edit(HttpServletRequest httpServletRequest) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            Tenant tenant = tenantApplication.getById(new ObjectId(tenant_id));
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.UPDATE_ACTION)
                    .build();
            tenant.setLast_updated_by(ref);
            tenant.setStatus(!tenant.getStatus());
            Tenant edited = tenantApplication.edit(tenant);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(edited).build();
            return Optional.of(res);
        }
        catch (Exception e){
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
