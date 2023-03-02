package com.happy_time.happy_time.ddd.check_attendance.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.ddd.check_attendance.AttendanceAgent;
import com.happy_time.happy_time.ddd.check_attendance.application.CheckAttendanceApplication;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandAttendance;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandGetAttendance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/attendance")
public class CheckAttendanceController {
    @Autowired
    private CheckAttendanceApplication checkAttendanceApplication;

    @Autowired
    private TokenUtils tokenUtils;

    @PostMapping("/check_attendance")
    public Optional<ResponseObject> checkAttendance(HttpServletRequest httpServletRequest, @RequestBody CommandAttendance attendance) throws Exception {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if (StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            attendance.setTenant_id(tenant_id);
            attendance.setAgent_id(agent_id);
            Long created = checkAttendanceApplication.attendance(attendance);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(created).build();
            ;
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/report/by_agent")
    public Optional<ResponseObject> reportByAgent(HttpServletRequest httpServletRequest, @RequestBody CommandGetAttendance command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if (StringUtils.isBlank(tenant_id) || StringUtils.isBlank(agent_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            command.setAgent_id(agent_id);
            command.setTenant_id(tenant_id);
            AttendanceAgent report = checkAttendanceApplication.reportByAgent(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(report).build();
            return Optional.of(res);

        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/tenant/report")
    public Optional<ResponseObject> reportForTenant(HttpServletRequest httpServletRequest, @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandGetAttendance command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            command.setTenant_id(tenant_id);
            command.setPage(page);
            command.setSize(size);
            List<AttendanceAgent> report = checkAttendanceApplication.reportByTenant(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(report).build();
            return Optional.of(res);

        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
