package com.happy_time.happy_time.ddd.configuration.head_position_config.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.Paginated;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.configuration.head_position_config.HeadPositionConfig;
import com.happy_time.happy_time.ddd.configuration.head_position_config.application.HeadPositionConfigApplication;
import com.happy_time.happy_time.jwt.JWTUtility;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/default/head_position")
public class HeadPositionConfigController {
    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private HeadPositionConfigApplication headPositionConfigApplication;

    @GetMapping("/get_all")
    public Optional<ResponseObject> getAll(HttpServletRequest httpServletRequest) {
        try {
           List<HeadPositionConfig> list = headPositionConfigApplication.getAll();
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(list).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody HeadPositionConfig config) {
        try {
            HeadPositionConfig created_config = headPositionConfigApplication.create(config);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(created_config).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
