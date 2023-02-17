package com.happy_time.happy_time.ddd.request_config.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.ddd.request_config.RequestConfig;
import com.happy_time.happy_time.ddd.request_config.application.RequestConfigApplication;
import com.happy_time.happy_time.ddd.shift_type.ShiftType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/request_config")
public class RequestConfigController {
    @Autowired
    private RequestConfigApplication requestConfigApplication;
    @Autowired
    private TokenUtils tokenUtils;
    @GetMapping("/get_all")
    public Optional<ResponseObject> getAll(HttpServletRequest httpServletRequest) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            List<RequestConfig> list = requestConfigApplication.getAllByTenant(tenant_id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(list).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody RequestConfig config) {
        try {
            RequestConfig requestConfig = requestConfigApplication.create(config);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(requestConfig).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
