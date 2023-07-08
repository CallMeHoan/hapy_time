package com.happy_time.happy_time.ddd.face_tracking.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.Paginated;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.face_tracking.FaceTracking;
import com.happy_time.happy_time.ddd.face_tracking.application.FaceTrackingApplication;
import com.happy_time.happy_time.ddd.face_tracking.command.CommandFaceTracking;
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
@RequestMapping(path = "/api/face_tracking")
public class FaceTrackingController {
    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private FaceTrackingApplication faceTrackingApplication;

    @PostMapping("/search")
    public Optional<ResponseObject> search(HttpServletRequest httpServletRequest, @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandFaceTracking command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id) || command == null) {
                throw new IllegalArgumentException(ExceptionMessage.MISSING_PARAMS);
            }
            command.setTenant_id(tenant_id);
            Page<FaceTracking> configs = faceTrackingApplication.search(command, page, size);
            List<FaceTracking> list = configs.getContent();
            if (configs.getTotalElements() > 0L) {
                Paginated<FaceTracking> total_configs = new Paginated<>(list, configs.getTotalPages(), configs.getSize(), configs.getTotalElements());
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            } else {
                Paginated<FaceTracking> total_configs = new Paginated<>(new ArrayList<>(), 0, 0, 0);
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            }
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody FaceTracking device) throws Exception {
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
            device.setLast_update_by(ref);
            device.setCreate_by(ref);
            device.setAgent_id(agent_id);
            FaceTracking created = faceTrackingApplication.create(device);
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

    @PutMapping("/update/{id}")
    public Optional<ResponseObject> update(HttpServletRequest httpServletRequest, @RequestBody CommandFaceTracking command,  @PathVariable ObjectId id) {
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
            command.setTenant_id(tenant_id);
            command.setAgent_id(agent_id);
            FaceTracking edited = faceTrackingApplication.update(command, id.toHexString());
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
            Boolean is_deleted = faceTrackingApplication.delete(id);
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
            FaceTracking config = faceTrackingApplication.getById(id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(config).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
