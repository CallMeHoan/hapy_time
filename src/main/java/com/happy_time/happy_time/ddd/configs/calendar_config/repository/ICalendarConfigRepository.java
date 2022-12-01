package com.happy_time.happy_time.ddd.configs.calendar_config.repository;

import com.happy_time.happy_time.ddd.configs.calendar_config.model.CalendarConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICalendarConfigRepository extends MongoRepository<CalendarConfig, String> {

}
