package com.happy_time.happy_time.ddd.shift_schedule.repository;

import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IShiftScheduleRepository extends MongoRepository<ShiftSchedule, String> {
}
