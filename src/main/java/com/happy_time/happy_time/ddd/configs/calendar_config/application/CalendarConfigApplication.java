package com.happy_time.happy_time.ddd.configs.calendar_config.application;

import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.configs.calendar_config.command.CommandCalendarConfig;
import com.happy_time.happy_time.ddd.configs.calendar_config.model.CalendarConfig;
import com.happy_time.happy_time.ddd.configs.calendar_config.repository.ICalendarConfigRepository;
import com.happy_time.happy_time.ddd.configs.device_config.command.CommandDeviceConfig;
import com.happy_time.happy_time.ddd.configs.device_config.model.DeviceConfig;
import org.apache.commons.lang3.BooleanUtils;
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

@Component
public class CalendarConfigApplication {
    @Autowired
    private ICalendarConfigRepository calendarConfigRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    public CalendarConfig create(CalendarConfig calendar_config) throws Exception {
        Boolean available_name = this.checkExist(calendar_config.getName(), "name", null, calendar_config.getTenant_id());
        if (available_name) {
            throw new Exception(ExceptionMessage.NAME_EXIST);
        }
        Boolean available_code = this.checkExist(calendar_config.getCalendar_code(), "calendar_code", null, calendar_config.getTenant_id());
        if (available_code) {
            throw new Exception(ExceptionMessage.CALENDAR_CODE_EXIST);
        }
        Long current = System.currentTimeMillis();
        calendar_config.setCreated_date(current);
        calendar_config.setLast_updated_date(current);
        calendarConfigRepository.save(calendar_config);
        return calendar_config;
    }

    public CalendarConfig update(CalendarConfig calendar_config) throws Exception {
        Boolean available_name = this.checkExist(calendar_config.getName(), "name", calendar_config.get_id().toString(), calendar_config.getTenant_id());
        if (available_name) {
            throw new Exception(ExceptionMessage.NAME_EXIST);
        }
        Boolean available_code = this.checkExist(calendar_config.getCalendar_code(), "calendar_code", calendar_config.get_id().toString(), calendar_config.getTenant_id());
        if (available_code) {
            throw new Exception(ExceptionMessage.CALENDAR_CODE_EXIST);
        }
        Query query = new Query();
        Long current_time = System.currentTimeMillis();
        query.addCriteria(Criteria.where("_id").is(calendar_config.get_id()));
        query.addCriteria(Criteria.where("tenant_id").is(calendar_config.getTenant_id()));
        Boolean is_exists = mongoTemplate.exists(query, CalendarConfig.class);
        if(is_exists) {
            calendar_config.setLast_updated_date(current_time);
            return mongoTemplate.save(calendar_config, "calendar_config");
        }
        else return null;
    }

    public CalendarConfig getById(ObjectId id) {
        CalendarConfig calendar_config = mongoTemplate.findById(id, CalendarConfig.class);
        if(calendar_config != null) {
            if (calendar_config.getIs_deleted()) return null;
            return calendar_config;
        } else return null;
    }

    public Boolean delete(ObjectId id) {
        Long current_time = System.currentTimeMillis();
        CalendarConfig calendar_config = mongoTemplate.findById(id, CalendarConfig.class);
        if(calendar_config != null) {
            calendar_config.setIs_deleted(true);
            calendar_config.setLast_updated_date(current_time);
            calendar_config.getLast_update_by().setAction(AppConstant.DELETE_ACTION);
            calendar_config.getLast_update_by().setUpdated_at(System.currentTimeMillis());
            mongoTemplate.save(calendar_config, "calendar_config");
            return true;
        } else return false;
    }

    public Page<CalendarConfig> search(CommandCalendarConfig command, Integer page, Integer size) throws Exception {
        List<CalendarConfig> calendar_configs = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        if(command == null) {
            throw new Exception(ExceptionMessage.INVALID_PARAMS);
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if(StringUtils.isNotBlank(command.getTenant_id())) {
            query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        }
        if(command.getIs_active() != null) {
            query.addCriteria(Criteria.where("is_active").is(command.getIs_active()));
        }
        if(StringUtils.isNotBlank(command.getCalendar_code())) {
            query.addCriteria(Criteria.where("calendar_code").regex(command.getCalendar_code(),"i"));
        }
        if(StringUtils.isNotBlank(command.getCalendar_name())) {
            query.addCriteria(Criteria.where("calendar_name").regex(command.getCalendar_name(),"i"));
        }

        calendar_configs = mongoTemplate.find(query, CalendarConfig.class);
        return PageableExecutionUtils.getPage(
                calendar_configs,
                pageRequest,
                () -> mongoTemplate.count(query, CalendarConfig.class));
    }

    public CalendarConfig getByTenant(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("is_active").is(true));
        if(StringUtils.isNotBlank(tenant_id)) {
            query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        }
        return mongoTemplate.find(query, CalendarConfig.class).get(0);
    }

    private Boolean checkExist(String src, String field_code, String id, String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("is_active").is(true));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        if (StringUtils.isNotBlank(id)) {
            query.addCriteria(Criteria.where("_id").is(id));
        }
        query.addCriteria(Criteria.where(field_code).regex(src,"i"));

        Long configs = mongoTemplate.count(query, CalendarConfig.class);
        if (StringUtils.isNotBlank(id) && configs > 0L) {
            return false;
        } else {
            return !StringUtils.isBlank(id) || configs != 0L;
        }
    }
}
