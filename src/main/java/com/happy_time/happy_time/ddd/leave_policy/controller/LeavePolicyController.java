package com.happy_time.happy_time.ddd.leave_policy.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.ddd.leave_policy.LeavePolicy;
import com.happy_time.happy_time.ddd.leave_policy.application.LeavePolicyApplication;
import com.happy_time.happy_time.ddd.leave_policy.command.CommandLeavePolicy;
import com.happy_time.happy_time.ddd.request_config.RequestConfig;
import com.happy_time.happy_time.ddd.request_config.command.CommandRequestConfig;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/leave_policy")
public class LeavePolicyController {
    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private LeavePolicyApplication leavePolicyApplication;

    @GetMapping("/get")
    public Optional<ResponseObject> getAll(HttpServletRequest httpServletRequest) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            LeavePolicy policy = leavePolicyApplication.getByTenant(tenant_id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(policy).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody LeavePolicy config) {
        try {
            LeavePolicy policy = leavePolicyApplication.create(config);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(policy).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PutMapping("/update")
    public Optional<ResponseObject> update(HttpServletRequest httpServletRequest, @RequestBody CommandLeavePolicy command){
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.UPDATE_ACTION)
                    .build();
            command.setRef(ref);
            LeavePolicy requestConfig = leavePolicyApplication.update(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(requestConfig).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
