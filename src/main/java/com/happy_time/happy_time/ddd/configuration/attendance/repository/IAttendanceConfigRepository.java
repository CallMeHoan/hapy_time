package com.happy_time.happy_time.ddd.configuration.attendance.repository;

import com.happy_time.happy_time.ddd.configuration.attendance.AttendanceConfig;
import com.happy_time.happy_time.ddd.configuration.head_position_config.HeadPositionConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAttendanceConfigRepository extends MongoRepository<AttendanceConfig, String> {
}
