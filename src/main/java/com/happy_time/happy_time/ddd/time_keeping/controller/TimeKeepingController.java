package com.happy_time.happy_time.ddd.time_keeping.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.time_keeping.application.TimeKeepingApplication;
import com.happy_time.happy_time.ddd.time_keeping.model.TimeKeeping;
import com.happy_time.happy_time.jwt.JWTUtility;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping(path = "/time_keeping")
public class TimeKeepingController {
    @Autowired
    private TimeKeepingApplication timeKeepingApplication;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private TokenUtils tokenUtils;

    @GetMapping("/get")
    public Optional<ResponseObject> get(HttpServletRequest httpServletRequest) throws Exception {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            TimeKeeping time_keeping = timeKeepingApplication.getByTenant(tenant_id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(time_keeping).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody TimeKeeping time_keeping) {
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
                .action(AppConstant.CREATE_ACTION)
                .build();
        time_keeping.setTenant_id(tenant_id);
        time_keeping.setLast_update_by(ref);
        time_keeping.setCreate_by(ref);
        TimeKeeping created = timeKeepingApplication.create(time_keeping);
        if(created != null) {
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(created).build();
            return Optional.of(res);
        } else {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(null).build();
            return Optional.of(res);
        }
    }

    @PutMapping("/update")
    public Optional<ResponseObject> edit(HttpServletRequest httpServletRequest, @RequestBody TimeKeeping time_keeping) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id) || StringUtils.isBlank(agent_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.UPDATE_ACTION)
                    .build();
            time_keeping.setLast_update_by(ref);
            TimeKeeping edited = timeKeepingApplication.update(time_keeping);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(edited).build();
            return Optional.of(res);
        }
        catch (Exception e){
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
