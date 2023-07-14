package com.happy_time.happy_time.ddd.attendance_report.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.ddd.attendance_report.AttendanceReport;
import com.happy_time.happy_time.ddd.attendance_report.application.AttendanceReportApplication;
import com.happy_time.happy_time.ddd.attendance_report.command.CommandAttendanceReport;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandAttendance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/attendance/report")
public class AttendanceReportController {
    @Autowired
    private AttendanceReportApplication attendanceReportApplication;

    @Autowired
    private TokenUtils tokenUtils;

    @PostMapping("/get")
    public Optional<ResponseObject> checkAttendance(HttpServletRequest httpServletRequest, @RequestBody CommandAttendanceReport command) throws Exception {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if (StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            command.setTenant_id(tenant_id);
            command.setAgent_id(agent_id);
            AttendanceReport report = attendanceReportApplication.search(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(report).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
