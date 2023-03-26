package com.happy_time.happy_time.ddd.check_attendance.application;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.common.DistanceUtils;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.command.CommandSearchAgent;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.attendance.AttendanceConfig;
import com.happy_time.happy_time.ddd.attendance.application.AttendanceConfigApplication;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.bssid_config.application.BssidConfigApplication;
import com.happy_time.happy_time.ddd.check_attendance.AttendanceAgent;
import com.happy_time.happy_time.ddd.check_attendance.CheckAttendance;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandGetAttendance;
import com.happy_time.happy_time.ddd.check_attendance.repository.ICheckAttendanceRepository;
import com.happy_time.happy_time.ddd.gps_config.GPSConfig;
import com.happy_time.happy_time.ddd.gps_config.application.GPSConfigApplication;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.ip_config.application.IPConfigApplication;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import com.happy_time.happy_time.ddd.check_attendance.command.CommandAttendance;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.application.ShiftScheduleApplication;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class CheckAttendanceApplication {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AgentApplication agentApplication;

    @Autowired
    private AttendanceConfigApplication attendanceConfigApplication;

    @Autowired
    private IPConfigApplication ipConfigApplication;

    @Autowired
    private BssidConfigApplication bssidConfigApplication;

    @Autowired
    private ShiftResultApplication shiftResultApplication;

    @Autowired
    private GPSConfigApplication gpsConfigApplication;

    @Autowired
    private ICheckAttendanceRepository iCheckAttendanceRepository;

    @Autowired
    private ShiftScheduleApplication shiftScheduleApplication;

    public Long attendance(CommandAttendance command) throws Exception {
        //check xem nhân viên + agent có tồn tại hay không
        if (StringUtils.isBlank(command.getTenant_id())) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        if (StringUtils.isBlank(command.getShift_id())) {
            throw new Exception(ExceptionMessage.USER_DO_NOT_HAVE_SHIFT_TODAY);
        }
        if (StringUtils.isBlank(command.getDevice_id())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Agent agent = null;
        if (StringUtils.isNotBlank(command.getAgent_id())) {
            agent = agentApplication.getById(new ObjectId(command.getAgent_id()));

        }
        if (agent == null) {
            throw new Exception(ExceptionMessage.AGENT_NOT_EXIST);
        }



        // check xem có cần chấm công không
        if (BooleanUtils.isNotTrue(agent.getRequired_attendance())) {
            throw new Exception(ExceptionMessage.AGENT_NOT_REQUIRED_CHECK_ATTENDANCE);
        }

        //lấy các cấu hình chấm công của doanh nghiệp để check xem sử dụng cấu hình nào
        AttendanceConfig config = attendanceConfigApplication.getByTenant(command.getTenant_id());
        if (config == null) {
            throw new Exception(ExceptionMessage.TENANT_DO_NOT_HAVE_ANY_ATTENDANCE_CONFIG);
        }

        String config_name = attendanceConfigApplication.getTenantAttendanceConfig(config);
        switch (config_name) {
            case "using_wifi":
                if (StringUtils.isBlank(command.getIp_address())) {
                    throw new Exception(ExceptionMessage.MISSING_PARAMS);
                }
                //get list ip config
                List<IPConfig> ip_configs = ipConfigApplication.getListActive(command.getTenant_id());
                if (CollectionUtils.isEmpty(ip_configs)) {
                    throw new Exception(ExceptionMessage.NO_CONFIG_FOUND);
                }
                IPConfig match_config = ip_configs.stream().filter(i -> i.getIp_address().equals(command.getIp_address())).findFirst().orElse(null);
                if (match_config == null) {
                    throw new Exception(ExceptionMessage.IP_ADDRESS_NOT_IN_CONFIG);
                }
                break;
            case "using_bssid_wifi":
                if (StringUtils.isBlank(command.getBssid_address())) {
                    throw new Exception(ExceptionMessage.MISSING_PARAMS);
                }
                List<BSSIDConfig> bssid_configs = bssidConfigApplication.getByTenant(command.getTenant_id());
                if (CollectionUtils.isEmpty(bssid_configs)) {
                    throw new Exception(ExceptionMessage.NO_CONFIG_FOUND);
                }
                BSSIDConfig match_bssid_config = bssid_configs.stream().filter(i -> i.getBssid_address().equals(command.getIp_address())).findFirst().orElse(null);
                if (match_bssid_config == null) {
                    throw new Exception(ExceptionMessage.BSSID_ADDRESS_NOT_IN_CONFIG);
                }
                break;
            case "using_gps":
                if (command.getLat() == null || command.getLon() == null) {
                    throw new Exception(ExceptionMessage.MISSING_PARAMS);
                }
                List<GPSConfig> gps_configs = gpsConfigApplication.getByTenant(command.getTenant_id());
                for (GPSConfig gps_config : gps_configs) {
                    if (BooleanUtils.isFalse(DistanceUtils.checkBelong(gps_config.getLat(), gps_config.getLon(), command.getLat(), command.getLon(), gps_config.getRadius()))) {
                        throw new Exception(ExceptionMessage.NOT_IN_RANGE);
                    }
                }
                break;
            case "using_qr_code":
            case "attendance_using_tracker":
            case "attendance_using_face_id":
                throw new Exception(ExceptionMessage.ATTENDANCE_METHOD_IS_NOT_SUPPORTED);
            case "no_limitation":
                break;
        }

        // sau khi check config xong => update shift result
        Long current = System.currentTimeMillis();
        ReferenceData ref = ReferenceData.builder()
                .updated_at(current)
                .agent_id(command.getAgent_id())
                .name(agent.getName())
                .action(command.getType().equals("check_in") ? AppConstant.CREATE_ACTION : AppConstant.UPDATE_ACTION)
                .build();
        ShiftResult shift = shiftResultApplication.getById(command.getShift_id());

        // Lấy ra shift để tính work count
        List<String> ids = shift.getShift().getShift_schedule_ids();
        if (CollectionUtils.isEmpty(ids)) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        ShiftSchedule schedule = shiftScheduleApplication.getById(ids.get(0));
        String current_date = DateTimeUtils.convertLongToDate("dd/MM/yyyy", current);

        switch (command.getType()) {
            case "check_in" -> {
                shift.setChecked_in_time(current);
                if(schedule.getConfig_in_late() == null) {
                    throw new Exception(ExceptionMessage.NO_CONFIG_FOUND);
                }
                boolean is_late = false;
                if (schedule.getConfig_in_late().getTime() != null) {
                    if (schedule.getConfig_in_late().getTime() != null) {
                        Long allow_in_time = DateTimeUtils.parseLongFromString(current_date + schedule.getConfig_in_late().getTime(), "dd/MM/yyyy HH:mm:SS");
                        if (allow_in_time < current) {
                            is_late = true;
                        }
                    } else if (schedule.getConfig_in_late().getLate_in_morning() != null) {
                        Long allow_in_time = DateTimeUtils.parseLongFromString(current_date + schedule.getConfig_in_late().getLate_in_morning(), "dd/MM/yyyy HH:mm:SS");
                        if (allow_in_time < current) {
                            is_late = true;
                        }
                    }

                }
                CheckAttendance check_in = CheckAttendance.builder()
                        .agent_id(command.getAgent_id())
                        .tenant_id(command.getTenant_id())
                        .create_by(ref)
                        .last_update_by(ref)
                        .checked_in_at(current)
                        .work_count(schedule.getPartial_work_count())
                        .attendance_date(current_date)
                        .is_late(is_late)
                        .build();
                this.create(check_in);
            }
            case "check_out" -> {
                shift.setChecked_out_time(current);
                if(schedule.getConfig_out_early() == null) {
                    throw new Exception(ExceptionMessage.NO_CONFIG_FOUND);
                }
                boolean is_out_early = false;
                if (schedule.getConfig_out_early().getTime() != null) {
                    if (schedule.getConfig_out_early().getTime() != null) {
                        Long allow_out_time = DateTimeUtils.parseLongFromString(current_date + schedule.getConfig_out_early().getTime(), "dd/MM/yyyy HH:mm:SS");
                        if (allow_out_time >= current) {
                            is_out_early = true;
                        }
                    } else if (schedule.getConfig_in_late().getLate_in_morning() != null) {
                        Long allow_in_time = DateTimeUtils.parseLongFromString(current_date + schedule.getConfig_in_late().getLate_in_morning(), "dd/MM/yyyy HH:mm:SS");
                        if (allow_in_time < current) {
                            is_out_early = true;
                        }
                    }

                }
                CheckAttendance check_out = this.search(command.getTenant_id(), command.getAgent_id());
                if (check_out != null) {
                    check_out.setChecked_out_at(current);
                    check_out.setWork_count(schedule.getWork_count());
                    check_out.setLast_update_by(ref);
                    check_out.setIs_check_out_soon(true);
                }
            }
        }
        shift.setLast_update_by(ref);
        shiftResultApplication.update(shift);
        return current;

    }

    public CheckAttendance create(CheckAttendance check_attendance) {
        Long current = System.currentTimeMillis();
        check_attendance.setCreated_date(current);
        check_attendance.setLast_updated_date(current);
        iCheckAttendanceRepository.insert(check_attendance);
        return check_attendance;
    }

    public CheckAttendance update(CheckAttendance check_attendance) {
        Long current = System.currentTimeMillis();
        check_attendance.setLast_updated_date(current);
        iCheckAttendanceRepository.insert(check_attendance);
        return check_attendance;
    }

    public CheckAttendance search(String tenant_id, String agent_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("agent_id").is(agent_id));
        query.addCriteria(Criteria.where("attendance_date").is(DateTimeUtils.convertLongToDate("dd/MM/yyyy", System.currentTimeMillis())));
        return mongoTemplate.findOne(query, CheckAttendance.class);
    }

    public List<CheckAttendance> findMany(CommandGetAttendance command) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
        if (StringUtils.isNotBlank(command.getAgent_id())) {
            query.addCriteria(Criteria.where("agent_id").is(command.getAgent_id()));
        }
        if (command.getFrom() != null && command.getTo() != null) {
            query.addCriteria(Criteria.where("created_date").gte(command.getFrom()).lte(command.getTo()));
        }
        return mongoTemplate.find(query, CheckAttendance.class);
    }

    public AttendanceAgent reportByAgent(CommandGetAttendance command) throws Exception {
        List<CheckAttendance> attendances = this.findMany(command);
        Agent agent = agentApplication.getById(new ObjectId(command.getAgent_id()));
        if (agent == null) {
            throw new Exception(ExceptionMessage.AGENT_NOT_EXIST);
        }
        AttendanceAgent res = AttendanceAgent.builder()
                .agent_id(command.getAgent_id())
                .agent_name(agent.getName())
                .gender(agent.getGender())
                .avatar(agent.getAvatar())
                .tenant_id(agent.getTenant_id())
                .build();
        List<AttendanceAgent.CheckAttendanceResult> attendance_results = new ArrayList<>();
        for (CheckAttendance attend : attendances) {
            AttendanceAgent.CheckAttendanceResult result = AttendanceAgent.CheckAttendanceResult.builder()
                    .attendance_date(attend.getAttendance_date())
                    .work_count(attend.getWork_count())
                    .checked_in_at(attend.getChecked_in_at())
                    .checked_out_at(attend.getChecked_out_at())
                    .build();
            attendance_results.add(result);
        }
        res.setCheck_attendance_results(attendance_results);
        return res;
    }

    public List<AttendanceAgent> reportByTenant(CommandGetAttendance command) throws Exception {
        CommandSearchAgent commandSearchAgent = CommandSearchAgent.builder().tenant_id(command.getTenant_id()).build();
        Page<Agent> searched = agentApplication.search(commandSearchAgent, command.getPage(), command.getSize());
        List<Agent> agents = searched.getContent();
        List<AttendanceAgent> list = new ArrayList<>();
        if (agents.size() == 0) {
            return new ArrayList<>();
        }
        for (Agent agent : agents) {
            CommandGetAttendance commandGetAttendance = CommandGetAttendance.builder()
                    .tenant_id(command.getTenant_id())
                    .agent_id(agent.get_id().toHexString())
                    .from(command.getFrom())
                    .to(command.getTo())
                    .build();
            List<CheckAttendance> attendances = this.findMany(commandGetAttendance);
            AttendanceAgent res = AttendanceAgent.builder()
                    .agent_id(command.getAgent_id())
                    .agent_name(agent.getName())
                    .gender(agent.getGender())
                    .avatar(agent.getAvatar())
                    .tenant_id(agent.getTenant_id())
                    .build();
            List<AttendanceAgent.CheckAttendanceResult> attendance_results = new ArrayList<>();
            for (CheckAttendance attend : attendances) {
                AttendanceAgent.CheckAttendanceResult result = AttendanceAgent.CheckAttendanceResult.builder()
                        .attendance_date(attend.getAttendance_date())
                        .work_count(attend.getWork_count())
                        .checked_in_at(attend.getChecked_in_at())
                        .checked_out_at(attend.getChecked_out_at())
                        .build();
                attendance_results.add(result);
            }
            res.setCheck_attendance_results(attendance_results);
            list.add(res);

        }
        return list;
    }
}