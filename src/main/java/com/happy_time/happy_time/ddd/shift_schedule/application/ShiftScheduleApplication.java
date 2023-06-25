package com.happy_time.happy_time.ddd.shift_schedule.application;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.bssid_config.command.CommandBssidConfig;
import com.happy_time.happy_time.ddd.department.Department;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.position.Position;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.command.CommandShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.command.CommandValidateShift;
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

import java.time.LocalDateTime;
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
            Criteria criteria = new Criteria();
            criteria.orOperator((Criteria.where("name_unsigned").regex(HAPStringUtils.stripAccents(command.getKeyword().toLowerCase(Locale.ROOT)),"i")), Criteria.where("code").regex(command.getKeyword()));
            query.addCriteria(criteria);
        }
        if (command.getIs_enabled() != null) {
            query.addCriteria(Criteria.where("is_enabled").is(command.getIs_enabled()));
        }
        if (StringUtils.isNotBlank(command.getShift_type_name())) {
            query.addCriteria(Criteria.where("shift_type.name").is(command.getShift_type_name()));
        }
        Long total = mongoTemplate.count(query, ShiftSchedule.class);
        schedules = mongoTemplate.find(query.with(pageRequest), ShiftSchedule.class);
        return PageableExecutionUtils.getPage(
                schedules,
                pageRequest,
                () -> total);
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

    private List<ShiftSchedule> getByIds(List<String> ids, String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").in(ids));
        query.addCriteria(Criteria.where("is_deleted").is(false));
        return mongoTemplate.find(query, ShiftSchedule.class);
    }


    public Boolean validateShift(CommandValidateShift command) throws Exception {
        List<ShiftSchedule> items = this.getByIds(command.getShift_ids(), command.getTenant_id());
        int size = items.size();
        if (size == 0) {
            throw new Exception("Vui lòng nhập ca làm việc");
        }
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                String shift_name_1 = items.get(i).getName();
                String shift_name_2 = items.get(j).getName();
                String start_1 = "", end_1 = "", start_2 = "", end_2 = "";
                //ca hành chính
                if (items.get(i).getMorning_working_time() != null
                        && items.get(i).getAfternoon_working_time() != null
                        && StringUtils.isNotBlank(items.get(i).getAfternoon_working_time().getFrom())
                        && StringUtils.isNotBlank(items.get(i).getAfternoon_working_time().getTo())) {
                    start_1 = items.get(i).getMorning_working_time().getFrom();
                    end_1 = items.get(i).getAfternoon_working_time().getTo();
                }
                // ca đơn
                else if (items.get(i).getWorking_time() != null) {
                    start_1 = items.get(i).getWorking_time().getFrom();
                    end_1 = items.get(i).getWorking_time().getTo();
                }

                //ca hành chính
                if (items.get(j).getMorning_working_time() != null
                        && items.get(j).getAfternoon_working_time() != null
                        && StringUtils.isNotBlank(items.get(j).getAfternoon_working_time().getFrom())
                        && StringUtils.isNotBlank(items.get(j).getAfternoon_working_time().getTo())) {
                    start_2 = items.get(j).getMorning_working_time().getFrom();
                    end_2 = items.get(j).getAfternoon_working_time().getTo();
                }
                // ca đơn
                else if (items.get(j).getWorking_time() != null) {
                    start_2 = items.get(j).getWorking_time().getFrom();
                    end_2 = items.get(j).getWorking_time().getTo();
                }


                if (StringUtils.isNotBlank(start_1)
                        && StringUtils.isNotBlank(end_1)
                        && StringUtils.isNotBlank(start_2)
                        && StringUtils.isNotBlank(end_2)) {
                    String current_day = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis());
                    Long start1 = DateTimeUtils.parseLongFromString( current_day+ " " + start_1, "dd/MM/yyyy HH:mm:SS");
                    Long start2 = DateTimeUtils.parseLongFromString( current_day+ " " + start_2, "dd/MM/yyyy HH:mm:SS");
                    Long end1 = DateTimeUtils.parseLongFromString( current_day+ " " + end_1, "dd/MM/yyyy HH:mm:SS");
                    Long end2 = DateTimeUtils.parseLongFromString( current_day+ " " + end_2, "dd/MM/yyyy HH:mm:SS");
                    if ((start1 < start2 && start2 < end1) && (start1 < end2 && end2 < end1) || start1.equals(start2) || end1.equals(end2)) {
                        String shift_1 = shift_name_1 + " [" + start_1.substring(0, 5) + " - " + end_1.substring(0, 5) + "]";
                        String shift_2 = shift_name_2 + " [" + start_2.substring(0, 5) + " - " + end_2.substring(0, 5) + "]";
                        throw new Exception("Ca làm việc " + shift_1 + " và " + shift_2 + " bị trùng đang trùng lặp. Vui lòng kiểm tra lại");
                    }
                } else {
                    throw new Exception("Tồn tại ca làm việc không hợp lệ, vui lòng kiểm tra lại cấu hình ca làm việc");
                }

            }
        }
        return true;
    }
}
