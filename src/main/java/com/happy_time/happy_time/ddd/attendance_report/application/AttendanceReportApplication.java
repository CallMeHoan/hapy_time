package com.happy_time.happy_time.ddd.attendance_report.application;

import com.happy_time.happy_time.ddd.attendance_report.AttendanceReport;
import com.happy_time.happy_time.ddd.attendance_report.command.CommandAttendanceReport;
import com.happy_time.happy_time.ddd.attendance_report.repository.IAttendanceReportRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class AttendanceReportApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IAttendanceReportRepository iAttendanceReportRepository;

    public AttendanceReport search(CommandAttendanceReport command) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if (StringUtils.isNotBlank(command.getAgent_id())) {
            query.addCriteria(Criteria.where("agent_id").is(command.getAgent_id()));
        }
        if (StringUtils.isNotBlank(command.getMonth())) {
            query.addCriteria(Criteria.where("month").is(command.getMonth()));
        }
        return mongoTemplate.findOne(query, AttendanceReport.class);
    }

    public AttendanceReport create(AttendanceReport attendanceReport) {
        Long current = System.currentTimeMillis();
        attendanceReport.setCreated_date(current);
        attendanceReport.setLast_updated_date(current);
        iAttendanceReportRepository.insert(attendanceReport);
        return attendanceReport;
    }

    public AttendanceReport update(AttendanceReport attendanceReport) {
        Long current = System.currentTimeMillis();
        attendanceReport.setLast_updated_date(current);
        iAttendanceReportRepository.save(attendanceReport);
        return attendanceReport;
    }
}
