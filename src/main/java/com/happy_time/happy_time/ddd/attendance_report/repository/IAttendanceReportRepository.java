package com.happy_time.happy_time.ddd.attendance_report.repository;

import com.happy_time.happy_time.ddd.attendance_report.AttendanceReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAttendanceReportRepository extends MongoRepository<AttendanceReport, String> {
}
