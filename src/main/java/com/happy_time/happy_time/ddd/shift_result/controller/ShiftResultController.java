package com.happy_time.happy_time.ddd.shift_result.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.ddd.agent.command.CommandSearchAgent;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.shift_result.CommandSearchShiftResult;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.ShiftResultView;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/shift/result")
public class ShiftResultController {
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private ShiftResultApplication shiftResultApplication;
    @GetMapping("/get/by_agent")
    public Optional<ResponseObject> getByAgent(HttpServletRequest httpServletRequest) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ShiftResult result = shiftResultApplication.getByAgent(tenant_id, agent_id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(result).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/get/by_tenant")
    public Optional<ResponseObject> getByTenant(HttpServletRequest httpServletRequest,@RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandSearchShiftResult command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            command.setTenant_id(tenant_id);
            command.setPage(page);
            command.setSize(size);
            List<ShiftResultView> result = shiftResultApplication.getByTenant(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(result).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
