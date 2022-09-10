package com.happy_time.happy_time.ddd.tenant.controller;

import com.happy_time.happy_time.ddd.tenant.application.TenantApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/tenant")
public class TenantController {
    @Autowired
    private TenantApplication tenantApplication;

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
}
