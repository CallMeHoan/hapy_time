package com.happy_time.happy_time.ddd.shift_result.application;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.common.DistanceUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.attendance.AttendanceConfig;
import com.happy_time.happy_time.ddd.attendance.application.AttendanceConfigApplication;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.bssid_config.application.BssidConfigApplication;
import com.happy_time.happy_time.ddd.gps_config.GPSConfig;
import com.happy_time.happy_time.ddd.gps_config.application.GPSConfigApplication;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.ip_config.application.IPConfigApplication;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.command.CommandAttendance;
import com.happy_time.happy_time.ddd.shift_result.repository.IShiftResultRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ShiftResultApplication {
    @Autowired
    private IShiftResultRepository iShiftResultRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AgentApplication agentApplication;

    @Autowired
    private AttendanceConfigApplication attendanceConfigApplication;

    @Autowired
    private BssidConfigApplication bssidConfigApplication;

    @Autowired
    private IPConfigApplication ipConfigApplication;

    @Autowired
    private GPSConfigApplication gpsConfigApplication;

    public void assignForAgents(ShiftAssignment config) throws Exception {
        if (StringUtils.isBlank(config.getApply_for())) {
            throw new Exception(ExceptionMessage.MISSING_PARAMS);
        }
        Long current = System.currentTimeMillis();
        List<Agent> agents = switch (config.getApply_for()) {
            case "company" -> agentApplication.getByTenant(config.getTenant_id());
            case "agent" -> agentApplication.getByIds(config.getAgents());
            case "department" -> agentApplication.getByDepartmentIds(config.getDepartments());
            case "position" -> agentApplication.getByPositionIds(config.getPositions());
            default -> new ArrayList<>();
        };
        List<String> agent_ids = agents.stream().map(i -> i.get_id().toHexString()).collect(Collectors.toList());


        List<ShiftResult.Shift> shifts = new ArrayList<>();
        //check xem đang sử dụng loại nào để tính ngày
        if (BooleanUtils.isTrue(config.getUse_specific_day()) && config.getDay_applied() != null) {
            if(!CollectionUtils.isEmpty(config.getDay_applied().getShifts())) {
                for (ShiftAssignment.Shift shift: config.getDay_applied().getShifts()) {
                    ShiftResult.Shift s = ShiftResult.Shift.builder()
                            .shift_schedule_ids(shift.getShift_ids())
                            .date(Objects.requireNonNull(DateTimeUtils.parseFromString(shift.getDate(), "dd-MM-yyyy")).getTime())
                            .build();
                    shifts.add(s);
                }
            }
        }
        List<ShiftResult> results = new ArrayList<>();
        for (String id: agent_ids) {
            for (ShiftResult.Shift shift : shifts) {
                ShiftResult res = ShiftResult.builder()
                        .tenant_id(config.getTenant_id())
                        .agent_id(id)
                        .create_by(config.getCreate_by())
                        .last_update_by(config.getLast_update_by())
                        .created_at(current)
                        .last_updated_at(current)
                        .shift(shift)
                        .build();
                results.add(res);
            }
        }
        mongoTemplate.insert(results, "shift_result");
    }

    public void getForAgentByDay(String tenant_id, String agent_id){

    }

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
        ShiftResult shift = this.getById(command.getShift_id());

        switch (command.getType()) {
            case "check_in" -> shift.setChecked_in_time(current);
            case "check_out" -> shift.setChecked_out_time(current);
        }

        //Lưu bảng mới



        return current;

    }

    public ShiftResult getById(String id) {
        return mongoTemplate.findById(id, ShiftResult.class);
    }

}
