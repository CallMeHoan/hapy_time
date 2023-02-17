package com.happy_time.happy_time.ddd.department.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.department.Department;
import com.happy_time.happy_time.ddd.department.DepartmentView;
import com.happy_time.happy_time.ddd.department.application.DepartmentApplication;
import com.happy_time.happy_time.ddd.department.command.CommandDepartment;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/department")
public class DepartmentController {
    @Autowired
    private DepartmentApplication departmentApplication;

    @Autowired
    private TokenUtils tokenUtils;

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody CommandDepartment command) throws Exception {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException(ExceptionMessage.TENANT_NOT_EXIST);
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.CREATE_ACTION)
                    .build();
            command.setTenant_id(tenant_id);
            command.setCreated_by(ref);
            command.setLast_updated_by(ref);
            Department created = departmentApplication.create(command);
            return Optional.of(ResponseObject.builder().status(9999).message("success").payload(created).build());
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @DeleteMapping("/delete/{id}")
    public Optional<ResponseObject> delete(HttpServletRequest httpServletRequest, @PathVariable ObjectId id) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException(ExceptionMessage.TENANT_NOT_EXIST);
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.DELETE_ACTION)
                    .build();
            Boolean is_deleted = departmentApplication.delete(id.toHexString(), ref);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(is_deleted).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("delete_agent_failed").build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get_by_tenant")
    public Optional<ResponseObject> getByTenant(HttpServletRequest httpServletRequest) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            DepartmentView department_view = departmentApplication.getDepartmentOfTenant(tenant_id);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(department_view).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("delete_agent_failed").build();
            return Optional.of(res);
        }
    }
}
