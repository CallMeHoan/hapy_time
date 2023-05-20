package com.happy_time.happy_time.ddd.shift_schedule.application;

import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.bssid_config.command.CommandBssidConfig;
import com.happy_time.happy_time.ddd.department.Department;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.command.CommandShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.repository.IShiftScheduleRepository;
import org.apache.commons.lang3.StringUtils;
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
public class ShiftScheduleApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IShiftScheduleRepository iShiftScheduleRepository;

    public ShiftSchedule create(ShiftSchedule schedule) throws Exception {
        //validate data
        if (StringUtils.isEmpty(schedule.getTenant_id())) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        if (StringUtils.isBlank(schedule.getName()) ||
                StringUtils.isBlank(schedule.getCode()) ||
                schedule.getPartial_work_count() == null ||
                schedule.getWork_count() == null) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        if (schedule.getShift_type() != null) {
            switch (schedule.getShift_type().getName()) {
                case "Ca đơn" -> {
                    if (schedule.getAllow_in_time() == null ||
                            schedule.getAllow_out_time() == null ||
                            schedule.getWorking_time() == null) {
                        throw new Exception(ExceptionMessage.MISSING_PARAMS);
                    }
                }
                case "Ca hành chính" -> {
                    if (schedule.getAfternoon_allow_in_time() == null ||
                            schedule.getAfternoon_allow_out_time() == null ||
                            schedule.getAfternoon_working_time() == null ||
                            schedule.getMorning_allow_in_time() == null ||
                            schedule.getMorning_allow_out_time() == null ||
                            schedule.getMorning_working_time() == null) {
                        throw new Exception(ExceptionMessage.MISSING_PARAMS);
                    }
                }
                default -> throw new Exception(ExceptionMessage.SHIFT_TYPE_NOT_EXIST);
            }
        }
        this.checkExist(schedule.getName(), "", schedule.getTenant_id());
        this.checkExist("", schedule.getCode(), schedule.getTenant_id());
        Long current_time = System.currentTimeMillis();
        String name_unsigned = HAPStringUtils.stripAccents(schedule.getName()).toLowerCase(Locale.ROOT);
        schedule.setName_unsigned(name_unsigned);
        schedule.setCreated_date(current_time);
        schedule.setLast_updated_date(current_time);
        return iShiftScheduleRepository.insert(schedule);
    }

    public ShiftSchedule getById(String id) {
        return mongoTemplate.findById(id, ShiftSchedule.class);
    }

    public ShiftSchedule update(String id, CommandShiftSchedule command) throws Exception {
        ShiftSchedule schedule = this.getById(id);
        if (schedule == null) {
            throw new Exception(ExceptionMessage.NOT_EXIST);
        }
        Long current_time = System.currentTimeMillis();
        if (StringUtils.isNotBlank(command.getName())) {
            this.checkExist(command.getName(), "", command.getTenant_id());
            String name_unsigned = HAPStringUtils.stripAccents(schedule.getName()).toLowerCase(Locale.ROOT);
            schedule.setName_unsigned(name_unsigned);
            schedule.setName(command.getName());
        }
        if (StringUtils.isNotBlank(command.getCode())){
            this.checkExist("", command.getCode(), command.getTenant_id());
            schedule.setCode(command.getCode());
        }
        if (command.getIs_enabled() != null) {
            schedule.setIs_enabled(command.getIs_enabled());
        }
        if (command.getWork_count() != null) {
            schedule.setWork_count(command.getWork_count());
        }
        if (command.getPartial_work_count() != null) {
            schedule.setPartial_work_count(command.getPartial_work_count());
        }
        if (command.getIs_using_check_in_limit() != null) {
            schedule.setIs_using_check_in_limit(command.getIs_using_check_in_limit());
        }
        if (command.getIs_using_check_out_limit() != null) {
            schedule.setIs_using_check_out_limit(command.getIs_using_check_out_limit());
        }
        if (command.getConfig_in_late() != null) {
            schedule.setConfig_in_late(command.getConfig_in_late());
        }
        if (command.getConfig_out_early() != null) {
            schedule.setConfig_out_early(command.getConfig_out_early());
        }
        if (command.getWorking_time() != null) {
            schedule.setWorking_time(command.getWorking_time());
        }
        if (command.getAllow_in_time() != null) {
            schedule.setAllow_in_time(command.getAllow_in_time());
        }
        if (command.getAllow_out_time() != null) {
            schedule.setAllow_out_time(command.getAllow_out_time());
        }
        if (command.getAfternoon_allow_in_time() != null) {
            schedule.setAfternoon_allow_in_time(command.getAfternoon_allow_in_time());
        }
        if (command.getAfternoon_allow_out_time() != null) {
            schedule.setAfternoon_allow_out_time(command.getAfternoon_allow_out_time());
        }
        if (command.getAfternoon_working_time() != null) {
            schedule.setAfternoon_working_time(command.getAfternoon_working_time());
        }
        if (command.getMorning_allow_in_time() != null) {
            schedule.setMorning_allow_in_time(command.getMorning_allow_in_time());
        }
        if (command.getMorning_allow_out_time() != null) {
            schedule.setMorning_allow_out_time(command.getMorning_allow_out_time());
        }
        if (command.getMorning_working_time() != null) {
            schedule.setMorning_working_time(command.getMorning_working_time());
        }
        schedule.setLast_updated_date(current_time);
        schedule.setLast_update_by(command.getLast_updated_by());
        return iShiftScheduleRepository.save(schedule);
    }

    public Page<ShiftSchedule> search(CommandShiftSchedule command, Integer page, Integer size) throws Exception {
        List<ShiftSchedule> schedules = new ArrayList<>();
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
            query.addCriteria(Criteria.where("name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i"));
        }

        schedules = mongoTemplate.find(query, ShiftSchedule.class);
        return PageableExecutionUtils.getPage(
                schedules,
                pageRequest,
                () -> mongoTemplate.count(query, ShiftSchedule.class));
    }

    private void checkExist(String name, String code, String tenant_id) throws Exception {
        Query query = new Query();
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        if (StringUtils.isNotBlank(name)) {
            query.addCriteria(Criteria.where("name").regex(name ,"i"));
        }
        if (StringUtils.isNotBlank(code)) {
            query.addCriteria(Criteria.where("code").regex(code ,"i"));
        }
        query.addCriteria(Criteria.where("is_deleted").is(false));
        if (mongoTemplate.exists(query, Department.class)) {
            if (StringUtils.isNotBlank(name)) {
                throw new Exception(ExceptionMessage.NAME_EXIST);
            } else if (StringUtils.isNotBlank(code)){
                throw new Exception(ExceptionMessage.CODE_EXIST);
            }
        }
    }


}
