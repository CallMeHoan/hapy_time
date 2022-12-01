package com.happy_time.happy_time.ddd.configs.ip_config.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.Paginated;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.configs.ip_config.application.IPConfigApplication;
import com.happy_time.happy_time.ddd.configs.ip_config.command.CommandIPConfig;
import com.happy_time.happy_time.ddd.configs.ip_config.model.IPConfig;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/ip_config")
public class IPConfigController {
    @Autowired
    private IPConfigApplication ipConfigApplication;
    @Autowired
    private TokenUtils tokenUtils;

    @PostMapping("/search")
    public Optional<ResponseObject> search(HttpServletRequest httpServletRequest, @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandIPConfig command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id) || command == null) {
                throw new IllegalArgumentException(ExceptionMessage.MISSING_PARAMS);
            }
            command.setTenant_id(tenant_id);
            Page<IPConfig> ipConfigs = ipConfigApplication.search(command, page, size);
            List<IPConfig> list = ipConfigs.getContent();
            if (ipConfigs.getTotalElements() > 0L) {
                Paginated<IPConfig> total_configs = new Paginated<>(list, ipConfigs.getTotalPages(), ipConfigs.getSize(), ipConfigs.getTotalElements());
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            } else {
                Paginated<IPConfig> total_configs = new Paginated<>(new ArrayList<>(), 0, 0, 0);
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            }
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody IPConfig ipConfig) throws Exception {
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
                    .action(AppConstant.CREATE_ACTION)
                    .build();
            ipConfig.setTenant_id(tenant_id);
            ipConfig.setLast_update_by(ref);
            ipConfig.setCreate_by(ref);
            IPConfig created = ipConfigApplication.create(ipConfig);
            ResponseObject res;
            if(created != null) {
                res = ResponseObject.builder().status(9999).message("success").payload("add_new_ip_successfully").build();
            } else {
                res = ResponseObject.builder().status(-9999).message("failed").payload("add_new_ip_failed").build();
            }
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PutMapping("/update")
    public Optional<ResponseObject> update(HttpServletRequest httpServletRequest, @RequestBody IPConfig ipConfig) {
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
            ipConfig.setLast_update_by(ref);
            IPConfig edited = ipConfigApplication.update(ipConfig);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(edited).build();
            return Optional.of(res);
        }
        catch (Exception e){
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @DeleteMapping("/delete/{id}")
    public Optional<ResponseObject> delete(HttpServletRequest httpServletRequest, @PathVariable ObjectId id) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            Boolean is_deleted = ipConfigApplication.delete(id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(is_deleted).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("delete_agent_failed").build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get/{id}")
    public Optional<ResponseObject> getById(HttpServletRequest httpServletRequest, @PathVariable ObjectId id) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            IPConfig ipConfig = ipConfigApplication.getById(id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(ipConfig).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
