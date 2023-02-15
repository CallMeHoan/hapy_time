package com.happy_time.happy_time.ddd.configuration.holiday_schedule.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.configuration.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.configuration.bssid_config.command.CommandBssidConfig;
import com.happy_time.happy_time.ddd.configuration.holiday_schedule.HolidaySchedule;
import com.happy_time.happy_time.ddd.configuration.holiday_schedule.command.CommandHolidaySchedule;
import com.happy_time.happy_time.ddd.configuration.holiday_schedule.repository.IHolidayScheduleRepository;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class HolidayScheduleApplication {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IHolidayScheduleRepository iHolidayScheduleRepository;

    public Page<HolidaySchedule> search(CommandHolidaySchedule command, Integer page, Integer size) throws Exception {
        List<HolidaySchedule> list = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(StringUtils.isNotBlank(command.getKeyword())) {
            query.addCriteria(Criteria.where("holiday_name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
        }

        list = mongoTemplate.find(query, HolidaySchedule.class);
        return PageableExecutionUtils.getPage(
                list,
                pageRequest,
                () -> mongoTemplate.count(query, HolidaySchedule.class));
    }

    public HolidaySchedule create(HolidaySchedule holiday) throws Exception {
        if (StringUtils.isBlank(holiday.getHoliday_name())
                || holiday.getDate_from() == null
                || holiday.getDate_to() == null) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        String name_unsigned = HAPStringUtils.stripAccents(holiday.getHoliday_name()).toLowerCase(Locale.ROOT);
        holiday.setHoliday_name_unsigned(name_unsigned);
        Long current = System.currentTimeMillis();
        holiday.setCreated_at(current);
        holiday.setLast_updated_at(current);
        iHolidayScheduleRepository.insert(holiday);
        return holiday;
    }

    public HolidaySchedule update(CommandHolidaySchedule command, String id) throws Exception {
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(id));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        HolidaySchedule holiday = mongoTemplate.findOne(query, HolidaySchedule.class);
        if(holiday != null) {
            if (StringUtils.isBlank(holiday.getHoliday_name())
                    || holiday.getDate_from() == null
                    || holiday.getDate_to() == null) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            String name_unsigned = HAPStringUtils.stripAccents(command.getHoliday_name()).toLowerCase(Locale.ROOT);
            holiday.setHoliday_name(command.getHoliday_name());
            holiday.setHoliday_name_unsigned(name_unsigned);
            holiday.setDate_from(command.getDate_from());
            holiday.setDate_to(command.getDate_to());
            holiday.setLast_updated_at(current_time);
            holiday.setLast_updated_by(command.getLast_updated_by());
            return mongoTemplate.save(holiday, "holiday_schedule");
        }
        else return null;
    }

    public HolidaySchedule getById(ObjectId id) {
        HolidaySchedule holiday = mongoTemplate.findById(id, HolidaySchedule.class);
        if(holiday != null) {
            if (holiday.getIs_deleted()) return null;
            return holiday;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        HolidaySchedule holiday = mongoTemplate.findById(id, HolidaySchedule.class);
        if(holiday != null) {
            holiday.setIs_deleted(true);
            holiday.setLast_updated_at(current_time);
            holiday.getLast_updated_by().setAction(AppConstant.DELETE_ACTION);
            holiday.getLast_updated_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(holiday, "holiday_schedule");
            return true;
        } else return false;
    }
}
