package com.happy_time.happy_time.ddd.face_tracking_account.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.ddd.face_tracking.FaceTracking;
import com.happy_time.happy_time.ddd.face_tracking.application.FaceTrackingApplication;
import com.happy_time.happy_time.ddd.face_tracking.command.CommandFaceTracking;
import com.happy_time.happy_time.ddd.face_tracking_account.FaceTrackingAccount;
import com.happy_time.happy_time.ddd.face_tracking_account.application.FaceTrackingAccountApplication;
import com.happy_time.happy_time.ddd.face_tracking_account.command.CommandFaceTrackingAccount;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class FaceTrackingAccountController {
    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private FaceTrackingAccountApplication faceTrackingAccountApplication;


    @PostMapping("/upsert")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody CommandFaceTrackingAccount device) throws Exception {
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
            device.setTenant_id(tenant_id);
            device.setRef(ref);
            FaceTrackingAccount created = faceTrackingAccountApplication.upsert(device);
            ResponseObject res;
            if(created != null) {
                res = ResponseObject.builder().status(9999).message("success").payload(created).build();
            } else {
                res = ResponseObject.builder().status(-9999).message("failed").payload("add_new_ip_failed").build();
            }
            return Optional.of(res);
        } catch (Exception e) {
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
            Boolean is_deleted = faceTrackingAccountApplication.delete(id);
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
            FaceTrackingAccount config = faceTrackingAccountApplication.getById(id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(config).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get/by_tenant")
    public Optional<ResponseObject> getByTenant(HttpServletRequest httpServletRequest, @PathVariable ObjectId id) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            CommandFaceTrackingAccount command = CommandFaceTrackingAccount.builder()
                    .tenant_id(tenant_id)
                    .build();
            FaceTrackingAccount config = faceTrackingAccountApplication.searchOne(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(config).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
