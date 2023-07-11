package com.happy_time.happy_time.ddd.check_attendance.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.Paginated;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.agent.model.AgentV0;
import com.happy_time.happy_time.ddd.check_attendance.AttendanceAgent;
import com.happy_time.happy_time.ddd.check_attendance.CheckAttendance;
import com.happy_time.happy_time.ddd.check_attendance.application.CheckAttendanceApplication;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandAttendance;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandAttendanceFaceTracking;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandGetAttendance;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandResultByFaceTracking;
import com.happy_time.happy_time.ddd.face_tracking.FaceTracking;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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
            Page<AttendanceAgent> report = checkAttendanceApplication.reportByTenant(command);
            List<AttendanceAgent> list = report.getContent();
            if (report.getTotalElements() > 0L) {
                Paginated<AttendanceAgent> total_configs = new Paginated<>(list, report.getTotalPages(), report.getSize(), report.getTotalElements());
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            } else {
                Paginated<AttendanceAgent> total_configs = new Paginated<>(new ArrayList<>(), 0, 0, 0);
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            }

        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }


    @GetMapping("/ranking")
    public Optional<ResponseObject> reportForTenant(HttpServletRequest httpServletRequest, @RequestParam("page") Integer page, @RequestParam("size") Integer size) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            Page<CheckAttendance> ranks = checkAttendanceApplication.rankingByTenant(tenant_id, page, size);
            if (ranks.getContent().size() > 0) {
                List<CheckAttendance> list = ranks.getContent();
                if (ranks.getTotalElements() > 0L) {
                    Paginated<CheckAttendance> total_agents = new Paginated<>(list, page, size, ranks.getTotalElements());
                    ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_agents).build();
                    return Optional.of(res);
                }
            }
            Paginated<Agent> total_agents = new Paginated<>(new ArrayList<>(), 0, 0, 0);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_agents).build();
            return Optional.of(res);

        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/check_attendance/face_tracking")
    public Optional<ResponseObject> checkAttendanceUsingFaceTracking(HttpServletRequest httpServletRequest, @RequestBody CommandAttendanceFaceTracking attendance) throws Exception {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            attendance.setTenant_id(tenant_id);
            CommandResultByFaceTracking created = checkAttendanceApplication.attendanceUsingFaceTracking(attendance);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(created).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public  ResponseEntity<byte[]> exportExcel(HttpServletRequest httpServletRequest, @RequestBody CommandGetAttendance command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            // Tạo workbook mới
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sheet 1");

            // Tạo dữ liệu mẫu
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Column 1");
            headerRow.createCell(1).setCellValue("Column 2");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Value 1");
            dataRow.createCell(1).setCellValue("Value 2");

            // Ghi workbook vào ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // Tạo header cho phản hồi HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "data.xlsx");

            // Trả về mảng byte của Excel file
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            HttpHeaders headers = new HttpHeaders();
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(null);
        }
    }
}
