package com.happy_time.happy_time.ddd.check_attendance.repository;

import com.happy_time.happy_time.ddd.check_attendance.CheckAttendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICheckAttendanceRepository extends MongoRepository<CheckAttendance, String> {
}
