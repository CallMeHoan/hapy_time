package com.happy_time.happy_time.ddd.holiday_schedule.repository;

import com.happy_time.happy_time.ddd.holiday_schedule.HolidaySchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IHolidayScheduleRepository extends MongoRepository<HolidaySchedule, String> {
}
