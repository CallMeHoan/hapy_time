package com.happy_time.happy_time.ddd.shift_schedule.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.Utils.TokenUtils;
import com.happy_time.happy_time.common.Paginated;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.holiday_schedule.HolidaySchedule;
import com.happy_time.happy_time.ddd.holiday_schedule.command.CommandHolidaySchedule;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.application.ShiftScheduleApplication;
import com.happy_time.happy_time.ddd.shift_schedule.command.CommandShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.command.CommandValidateShift;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/shift_schedule")
public class ShiftScheduleController {
    @Autowired
    private ShiftScheduleApplication shiftScheduleApplication;
    @Autowired
    private TokenUtils tokenUtils;
    @PostMapping("/search")
    public Optional<ResponseObject> search(HttpServletRequest httpServletRequest, @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestBody CommandShiftSchedule command) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if (StringUtils.isBlank(tenant_id) || command == null) {
                throw new IllegalArgumentException(ExceptionMessage.MISSING_PARAMS);
            }
            command.setTenant_id(tenant_id);
            Page<ShiftSchedule> schedules = shiftScheduleApplication.search(command, page, size);
            List<ShiftSchedule> list = schedules.getContent();
            if (schedules.getTotalElements() > 0L) {
                Paginated<ShiftSchedule> total_configs = new Paginated<>(list, schedules.getTotalPages(), schedules.getSize(), schedules.getTotalElements());
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            } else {
                Paginated<IPConfig> total_configs = new Paginated<>(new ArrayList<>(), 0, 0, 0);
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(total_configs).build();
                return Optional.of(res);
            }
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody ShiftSchedule schedule) throws Exception {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.CREATE_ACTION)
                    .build();
            schedule.setTenant_id(tenant_id);
            schedule.setLast_update_by(ref);
            schedule.setCreate_by(ref);
            ShiftSchedule created = shiftScheduleApplication.create(schedule);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(created).build();;
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PutMapping("/update/{id}")
    public Optional<ResponseObject> update(HttpServletRequest httpServletRequest, @RequestBody CommandShiftSchedule command,@PathVariable ObjectId id) {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            String agent_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "agent_id");
            String name = tokenUtils.getFieldValueThroughToken(httpServletRequest, "name");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("missing_params");
            }
            ReferenceData ref = ReferenceData.builder()
                    .agent_id(agent_id)
                    .updated_at(System.currentTimeMillis())
                    .name(name)
                    .action(AppConstant.UPDATE_ACTION)
                    .build();
            command.setLast_updated_by(ref);
            ShiftSchedule edited = shiftScheduleApplication.update(id.toHexString(), command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(edited).build();
            return Optional.of(res);
        }
        catch (Exception e){
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get/{id}")
    public Optional<ResponseObject> getById(HttpServletRequest httpServletRequest, @PathVariable ObjectId id) {
        try {
            ShiftSchedule schedule = shiftScheduleApplication.getById(id.toHexString());
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(schedule).build();
            return Optional.of(res);
        }
        catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/validate")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody CommandValidateShift command) throws Exception {
        try {
            String tenant_id = tokenUtils.getFieldValueThroughToken(httpServletRequest, "tenant_id");
            if(StringUtils.isBlank(tenant_id)) {
                throw new IllegalArgumentException("tenant not exist");
            }
            if (CollectionUtils.isEmpty(command.getShift_ids())) {
                throw new IllegalArgumentException("missing_params");
            }
            command.setTenant_id(tenant_id);
            Boolean result = shiftScheduleApplication.validateShift(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(result).build();;
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
