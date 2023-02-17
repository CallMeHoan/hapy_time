package com.happy_time.happy_time.ddd.attendance.repository;

import com.happy_time.happy_time.ddd.attendance.AttendanceConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAttendanceConfigRepository extends MongoRepository<AttendanceConfig, String> {
}
