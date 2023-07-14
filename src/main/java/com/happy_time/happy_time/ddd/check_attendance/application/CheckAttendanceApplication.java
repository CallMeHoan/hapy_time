package com.happy_time.happy_time.ddd.check_attendance.application;

import com.happy_time.happy_time.Utils.JsonUtils;
import com.happy_time.happy_time.Utils.ResponseObject;
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
import com.happy_time.happy_time.ddd.attendance_report.AttendanceReport;
import com.happy_time.happy_time.ddd.attendance_report.application.AttendanceReportApplication;
import com.happy_time.happy_time.ddd.bssid_config.BSSIDConfig;
import com.happy_time.happy_time.ddd.bssid_config.application.BssidConfigApplication;
import com.happy_time.happy_time.ddd.check_attendance.AttendanceAgent;
import com.happy_time.happy_time.ddd.check_attendance.CheckAttendance;
import com.happy_time.happy_time.ddd.check_attendance.command.*;
import com.happy_time.happy_time.ddd.check_attendance.repository.ICheckAttendanceRepository;
import com.happy_time.happy_time.ddd.department.Department;
import com.happy_time.happy_time.ddd.department.application.DepartmentApplication;
import com.happy_time.happy_time.ddd.device.Device;
import com.happy_time.happy_time.ddd.device.application.DeviceApplication;
import com.happy_time.happy_time.ddd.face_tracking.FaceTracking;
import com.happy_time.happy_time.ddd.face_tracking.application.FaceTrackingApplication;
import com.happy_time.happy_time.ddd.face_tracking_account.command.CommandFaceTrackingAccount;
import com.happy_time.happy_time.ddd.gps_config.GPSConfig;
import com.happy_time.happy_time.ddd.gps_config.application.GPSConfigApplication;
import com.happy_time.happy_time.ddd.ip_config.IPConfig;
import com.happy_time.happy_time.ddd.ip_config.application.IPConfigApplication;
import com.happy_time.happy_time.ddd.jedis.JedisMaster;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.application.ShiftScheduleApplication;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.happy_time.happy_time.ddd.face_tracking.application.FaceTrackingApplication.PYTHON_URL;
import static com.happy_time.happy_time.ddd.jedis.JedisMaster.COLON;
import static com.happy_time.happy_time.ddd.jedis.JedisMaster.JedisPrefixKey.shift_agent_key;

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

    @Autowired
    private FaceTrackingApplication faceTrackingApplication;

    @Autowired
    private JedisMaster jedisMaster;

    @Autowired
    private DepartmentApplication departmentApplication;

    @Autowired
    private DeviceApplication deviceApplication;

    @Autowired
    private AttendanceReportApplication attendanceReportApplication;

    public Long attendance(CommandAttendance command) throws Exception {
        //check xem nhân viên + agent có tồn tại hay không
        if (StringUtils.isBlank(command.getTenant_id())) {
            throw new Exception(ExceptionMessage.TENANT_NOT_EXIST);
        }
        if (StringUtils.isBlank(command.getShift_id())) {
            throw new Exception(ExceptionMessage.USER_DO_NOT_HAVE_SHIFT_TODAY);
        }
        if (StringUtils.isBlank(command.getDevice_id())) {
            throw new Exception("Không nhận được device id");
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
        Device device = deviceApplication.getByAgent(command.getAgent_id(), command.getTenant_id(), command.getDevice_id());
        switch (config_name) {
            case "using_wifi":
                if (StringUtils.isBlank(command.getIp_address())) {
                    throw new Exception("Không nhận được địa chỉ IP");
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
                if (device == null) {
                    throw new Exception("Vui lòng sử dụng đúng thiết bị của bạn để chấm công.");
                }
                break;
            case "using_bssid_wifi":
                if (StringUtils.isBlank(command.getBssid_address())) {
                    throw new Exception("Không nhận được BSSID");
                }
                List<BSSIDConfig> bssid_configs = bssidConfigApplication.getByTenant(command.getTenant_id());
                if (CollectionUtils.isEmpty(bssid_configs)) {
                    throw new Exception(ExceptionMessage.NO_CONFIG_FOUND);
                }
                BSSIDConfig match_bssid_config = bssid_configs.stream().filter(i -> i.getBssid_address().equals(command.getIp_address())).findFirst().orElse(null);
                if (match_bssid_config == null) {
                    throw new Exception(ExceptionMessage.BSSID_ADDRESS_NOT_IN_CONFIG);
                }
                if (device == null) {
                    throw new Exception("Vui lòng sử dụng đúng thiết bị của bạn để chấm công.");
                }
                break;
            case "using_gps":
                if (command.getLat() == null || command.getLon() == null) {
                    throw new Exception("Không nhận được tọa độ");
                }
                List<GPSConfig> gps_configs = gpsConfigApplication.getByTenant(command.getTenant_id());
                for (GPSConfig gps_config : gps_configs) {
                    if (BooleanUtils.isFalse(DistanceUtils.checkBelong(gps_config.getLat(), gps_config.getLon(), command.getLat(), command.getLon(), gps_config.getRadius()))) {
                        throw new Exception(ExceptionMessage.NOT_IN_RANGE);
                    }
                }
                if (device == null) {
                    throw new Exception("Vui lòng sử dụng đúng thiết bị của bạn để chấm công");
                }
                break;
            case "face_tracking":
                if (StringUtils.isNotBlank(command.getSource()) && "mobile".equals(command.getSource())) {
                    throw new Exception("Vui lòng sử dụng camera của công ty để chấm công");
                }
                if (StringUtils.isBlank(command.getImage())) {
                    throw new Exception(ExceptionMessage.MISSING_PARAMS);
                }
                break;
            case "using_qr_code":
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
        System.out.println("type:" + command.getType());
        switch (command.getType()) {
            case "check_in" -> {
                //check giới hạn chấm công ca đơn
                if (schedule.getAllow_in_time() != null
                        && StringUtils.isNotBlank(schedule.getAllow_in_time().getFrom())
                        && StringUtils.isNotBlank(schedule.getAllow_in_time().getTo())) {
                    Long allow_from = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getAllow_in_time().getFrom(), "dd/MM/yyyy HH:mm:SS");
                    Long allow_to = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getAllow_in_time().getTo(), "dd/MM/yyyy HH:mm:SS");
                    if (!(allow_from < current && current < allow_to)) {
                        throw new Exception(ExceptionMessage.NOT_IN_CHECK_IN_TIME);
                    }
                }

                //check giới hạn chấm công ca hành chính
                if (schedule.getMorning_allow_in_time() != null
                        && StringUtils.isNotBlank(schedule.getMorning_allow_in_time().getFrom())
                        && StringUtils.isNotBlank(schedule.getMorning_allow_in_time().getTo())) {
                    Long allow_from = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getMorning_allow_in_time().getFrom(), "dd/MM/yyyy HH:mm:SS");
                    Long allow_to = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getMorning_allow_in_time().getTo(), "dd/MM/yyyy HH:mm:SS");
                    if (!(allow_from < current && current < allow_to)) {
                        throw new Exception(ExceptionMessage.NOT_IN_CHECK_IN_TIME);
                    }
                }
                shift.setChecked_in_time(current);
                if (schedule.getConfig_in_late() == null) {
                    throw new Exception(ExceptionMessage.NO_CONFIG_FOUND);
                }
                boolean is_late = false;
                if (schedule.getConfig_in_late().getTime() != null) {
                    Long allow_in_time = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getConfig_in_late().getTime(), "dd/MM/yyyy HH:mm:SS");
                    if (allow_in_time < current) {
                        is_late = true;
                    }
                } else if (schedule.getConfig_in_late().getLate_in_morning() != null) {
                    Long allow_in_time = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getConfig_in_late().getLate_in_morning(), "dd/MM/yyyy HH:mm:SS");
                    if (allow_in_time < current) {
                        is_late = true;
                    }
                }
                //count tổng số record để set position
                int pos = (int) this.count(command.getTenant_id());
                CheckAttendance check_in = CheckAttendance.builder()
                        .agent_id(command.getAgent_id())
                        .tenant_id(command.getTenant_id())
                        .create_by(ref)
                        .last_update_by(ref)
                        .checked_in_at(current)
                        .work_count(schedule.getPartial_work_count())
                        .attendance_date(current_date)
                        .is_late(is_late)
                        .position(pos + 1)
                        .build();
                CheckAttendance check = this.create(check_in);
                System.out.println("Check in res:" + check);
                shift.setIs_late(is_late);

                //lưu thêm rank trên redis
                String key = JedisMaster.JedisPrefixKey.ranking_tenant_agent + COLON + command.getTenant_id() + COLON + command.getAgent_id() + COLON + current_date;
                Map<String, String> value = new HashMap<>();
                value.put("position", String.valueOf(pos + 1));
                jedisMaster.hSetAll(key, value);
            }
            case "check_out" -> {
                //check giới hạn chấm công ca đơn
                if (schedule.getAllow_in_time() != null
                        && StringUtils.isNotBlank(schedule.getAllow_out_time().getFrom())
                        && StringUtils.isNotBlank(schedule.getAllow_out_time().getTo())) {
                    Long allow_from = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getAllow_out_time().getFrom(), "dd/MM/yyyy HH:mm:SS");
                    Long allow_to = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getAllow_out_time().getTo(), "dd/MM/yyyy HH:mm:SS");
                    if (!(allow_from < current && current < allow_to)) {
                        throw new Exception(ExceptionMessage.NOT_IN_CHECK_OUT_TIME);
                    }
                }


                //check giới hạn chấm công ca hành chính
                if (schedule.getAfternoon_allow_out_time() != null
                        && StringUtils.isNotBlank(schedule.getAfternoon_allow_out_time().getFrom())
                        && StringUtils.isNotBlank(schedule.getAfternoon_allow_out_time().getTo())) {
                    Long allow_from = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getAfternoon_allow_out_time().getFrom(), "dd/MM/yyyy HH:mm:SS");
                    Long allow_to = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getAfternoon_allow_out_time().getTo(), "dd/MM/yyyy HH:mm:SS");
                    if (!(allow_from < current && current < allow_to)) {
                        throw new Exception(ExceptionMessage.NOT_IN_CHECK_OUT_TIME);
                    }
                }

                shift.setChecked_out_time(current);
                if (schedule.getConfig_out_early() == null) {
                    throw new Exception(ExceptionMessage.NO_CONFIG_FOUND);
                }
                boolean is_out_early = false;
                if (schedule.getConfig_out_early().getTime() != null) {
                    Long allow_out_time = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getConfig_out_early().getTime(), "dd/MM/yyyy HH:mm:SS");
                    if (allow_out_time >= current) {
                        is_out_early = true;
                    }
                } else if (schedule.getConfig_out_early().getEarly_out_afternoon() != null) {
                    Long allow_in_time = DateTimeUtils.parseLongFromString(current_date + " " + schedule.getConfig_out_early().getEarly_out_afternoon(), "dd/MM/yyyy HH:mm:SS");
                    if (allow_in_time < current) {
                        is_out_early = true;
                    }
                }
                CheckAttendance check_out = this.search(command.getTenant_id(), command.getAgent_id());
                if (check_out != null) {
                    check_out.setChecked_out_at(current);
                    check_out.setWork_count(schedule.getWork_count());
                    check_out.setLast_update_by(ref);
                    check_out.setIs_check_out_soon(is_out_early);
                    this.update(check_out);
                    shift.setIs_check_out_soon(is_out_early);
                }
            }
            default -> throw new Exception("Missing type");
        }
        shift.setLast_update_by(ref);
        shiftResultApplication.update(shift);

        //lưu lại report chấm công

        return current;

    }

    public CommandResultByFaceTracking attendanceUsingFaceTracking(CommandAttendanceFaceTracking command) throws Exception {
        //check xem ảnh đã hợp lệ hay chưa
        Long total_agent = agentApplication.countTotalByTenant(command.getTenant_id());
        Integer size = 100;
        Integer total_page = Math.toIntExact(total_agent / size);
        String agent_id = "";
        String shift_id = "";
        Map<String, String> map = new HashMap<>();
        String url = PYTHON_URL + "/check/face_recognition";
        for (int i = 0; i <= total_page; i++) {
            CommandSearchAgent commandSearchAgent = CommandSearchAgent.builder()
                    .tenant_id(command.getTenant_id())
                    .build();
            Page<Agent> agents = agentApplication.search(commandSearchAgent, i, size);
            if (agents.getContent().size() > 0) {
                List<Agent> list_agents = agents.getContent();
                //gọi api sang bên kia
                for (Agent agent : list_agents) {
                    FaceTracking faceTracking = faceTrackingApplication.getByAgentId(agent.get_id().toHexString(), command.getTenant_id());
                    //gọi API
                    if (faceTracking != null && !CollectionUtils.isEmpty(faceTracking.getFace_tracking_images())) {
                        map.put("image_url", faceTracking.getFace_tracking_images().get(0));
                        map.put("defined_image", command.getImage());
                        String json_body = JsonUtils.toJSON(map);
                        String res = faceTrackingApplication.callApi(url, json_body);
                        if (StringUtils.isBlank(res)) {
                            throw new Exception("Có lỗi xảy ra");
                        }
                        ResponseObject responseObject = JsonUtils.toObject(res, ResponseObject.class);
                        if (responseObject.getStatus() == -9999) {
                            throw new Exception("Có lỗi xảy ra");
                        }

                        //check nếu có images bị lỗi thì trả ra kết quả những hình ảnh bị lỗi kèm message
                        String payload = JsonUtils.toJSON(responseObject.getPayload());
                        if (payload.contains("true")) {
                            agent_id = agent.get_id().toHexString();
                            //từ đoạn này là kiếm shift id gắn dô là xong
                            ShiftResult shiftResult = shiftResultApplication.getByAgent(command.getTenant_id(), agent_id);
                            if (shiftResult != null) {
                                shift_id = shiftResult.get_id().toHexString();
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (StringUtils.isBlank(agent_id)) {
            throw new Exception("Nhân viên chưa tích hợp Face Tracking");
        }
        String key = shift_agent_key + COLON + shift_id + COLON + agent_id;
        System.out.println("agent_id: " + agent_id);
        System.out.println("shift_id: " + shift_id);
        String type = "check_in";
        Map<String, String> key_res = jedisMaster.hgetAll(key);
        System.out.println("key_res: " + key_res);
        if (key_res.size() > 0) {
            type = "check_out";
        } else {
            jedisMaster.hSet(key, "flag", "true");
        }
        //build command cuả hàm chấm công -> gọi làm hàm
        System.out.println("type CheckAttendanceApplication: " + type);
        CommandAttendance commandAttendance = CommandAttendance.builder()
                .tenant_id(command.getTenant_id())
                .image(command.getImage())
                .agent_id(agent_id)
                .shift_id(shift_id)
                .type(type)
                .device_id("face_tracking")
                .build();
        Long time = this.attendance(commandAttendance);
        Agent agent = agentApplication.getById(new ObjectId(agent_id));
        Department department = departmentApplication.getById(agent.getDepartment_id());
        return CommandResultByFaceTracking.builder()
                .attendance_time(time)
                .type(type)
                .agent_name(agent.getName())
                .department_name(department != null ? department.getDepartment_name() : null)
                .build();

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
        iCheckAttendanceRepository.save(check_attendance);
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

    public long count(String tenant_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("attendance_date").is(DateTimeUtils.convertLongToDate("dd/MM/yyyy", System.currentTimeMillis())));
        return mongoTemplate.count(query, CheckAttendance.class);
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
                    .is_check_out_soon(attend.getIs_check_out_soon())
                    .is_late(attend.getIs_late())
                    .build();
            attendance_results.add(result);
        }
        res.setCheck_attendance_results(attendance_results);
        return res;
    }

    public Page<AttendanceAgent> reportByTenant(CommandGetAttendance command) throws Exception {
        Pageable pageRequest = PageRequest.of(command.getPage(), command.getSize());
        CommandSearchAgent commandSearchAgent = CommandSearchAgent.builder().tenant_id(command.getTenant_id()).build();
        Page<Agent> searched = agentApplication.search(commandSearchAgent, command.getPage(), command.getSize());
        List<Agent> agents = searched.getContent();
        List<AttendanceAgent> list = new ArrayList<>();
        if (agents.size() == 0) {
            return null;
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
                    .agent_name(agent.getName())
                    .gender(agent.getGender())
                    .avatar(agent.getAvatar())
                    .tenant_id(agent.getTenant_id())
                    .agent_id(agent.get_id().toHexString())
                    .build();
            List<AttendanceAgent.CheckAttendanceResult> attendance_results = new ArrayList<>();
            for (CheckAttendance attend : attendances) {
                AttendanceAgent.CheckAttendanceResult result = AttendanceAgent.CheckAttendanceResult.builder()
                        .attendance_date(attend.getAttendance_date())
                        .work_count(attend.getWork_count())
                        .checked_in_at(attend.getChecked_in_at())
                        .checked_out_at(attend.getChecked_out_at())
                        .id(attend.get_id().toHexString())
                        .is_late(attend.getIs_late())
                        .is_check_out_soon(attend.getIs_check_out_soon())
                        .build();
                attendance_results.add(result);
            }
            res.setCheck_attendance_results(attendance_results);
            list.add(res);

        }
        return PageableExecutionUtils.getPage(
                list,
                pageRequest,
                searched::getTotalElements);
    }

    public Page<CheckAttendance> rankingByTenant(String tenant_id, Integer page, Integer size) {
        List<CheckAttendance> list = new ArrayList<>();
        String cur = DateTimeUtils.convertLongToDate("dd/MM/yyyy", System.currentTimeMillis());
        Pageable pageRequest = PageRequest.of(page, size);
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("attendance_date").is(cur));
        long total = mongoTemplate.count(query, CheckAttendance.class);
        if (total > 0) {
            query.with(Sort.by(Sort.Direction.ASC, "position"));
            list = mongoTemplate.find(query.with(pageRequest), CheckAttendance.class);
            //setview for agent
            this.setViewForAgent(list);
        }

        return PageableExecutionUtils.getPage(
                list,
                pageRequest,
                () -> total);

    }

    private void setViewForAgent(List<CheckAttendance> list) {
        if (!CollectionUtils.isEmpty(list)) {
            for (CheckAttendance item : list) {
                if (StringUtils.isNotBlank(item.getAgent_id())) {
                    item.setAgent_view(agentApplication.setView(item.getAgent_id(), item.getTenant_id()));
                }
                //set rank
                Integer dif = calculateRank(item.getTenant_id(), item.getAgent_id(), item.getPosition());
                //case > 0 có nghĩa là đi muộn :v
                if (dif > 0) {
                    item.setDown(dif);
                }
                //case > 0 có nghĩa là đí sơm
                else if (dif < 0) {
                    item.setUp(-dif);
                }
                //còn nếu bằng 0 thì là khong thay đổi gì cả -> up và down đều là null
            }
        }
    }

    private Integer calculateRank(String tenant_id, String agent_id, Integer position) {
        //lấy giá trị position của ngày trước đó -> tính toán
        Integer rank = 0;
        String date = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis() - JedisMaster.TimeUnit.one_day);
        String key = JedisMaster.JedisPrefixKey.ranking_tenant_agent + COLON + tenant_id + COLON + agent_id + COLON + date;
        Map<String, String> res = jedisMaster.hgetAll(key);
        if (res.size() > 0) {
            String last_day = res.get("position");
            if (StringUtils.isNotBlank(last_day)) {
                Integer pos = Integer.valueOf(last_day);
                rank = position - pos;
            }
        }
        return rank;
    }

    public CommandReportExcel exportExcel(String tenant_id, Long from, Long to) throws Exception {
        List<AttendanceAgent> list = new ArrayList<AttendanceAgent>();
        Long total_agents = agentApplication.countTotalByTenant(tenant_id);
        int size = 50;
        int total_page = (int) (total_agents / size);

        for (int i = 0; i <= total_page; i++) {
            CommandGetAttendance command = CommandGetAttendance.builder()
                    .from(from)
                    .to(to)
                    .tenant_id(tenant_id)
                    .page(i)
                    .size(50)
                    .build();
            Page<AttendanceAgent> item = this.reportByTenant(command);
            if (item.getSize() > 0) {
                list.addAll(item.getContent());
            }
        }
        //tính 2 số nagayf để tạo ra sỗ cột
        Integer total_column = DateTimeUtils.dayBetweenDates(from, to) + 1; // +1 để thêm tên nhân viên
        // Tạo workbook mới
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bảng công");

        //build header
        Row headerRow = sheet.createRow(0);
        Long start_day = from;
        for (int i = 0; i < total_column; i++) {
            if (i == 0) {
                headerRow.createCell(i).setCellValue("Tên nhân viên");
            } else {
                String current_day = DateTimeUtils.convertLongToDate("dd/MM/yyyy", start_day);
                start_day += JedisMaster.TimeUnit.one_day;
                headerRow.createCell(i).setCellValue(current_day);
            }

        }
        if (!CollectionUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                Long begin = from;
                //cộng 1 là vì bắt đầu tạo data từ dòng thứ 2 (dòng 1 là header)
                Row dataRow = sheet.createRow(i + 1);
                for (int j = 0; j < total_column; j ++) {
                    if (j == 0) {
                        dataRow.createCell(j).setCellValue(list.get(i).getAgent_name());
                    } else {
                        String current_day = DateTimeUtils.convertLongToDate("dd/MM/yyyy", begin);
                        String value = "";
                        if (!CollectionUtils.isEmpty(list.get(i).getCheck_attendance_results())) {
                            //check xem có ngày nào trong list không
                            AttendanceAgent.CheckAttendanceResult res = list.get(i).getCheck_attendance_results().stream().filter(record -> current_day.equals(record.getAttendance_date())).findFirst().orElse(null);
                            if (res != null) {
                                value = res.getWork_count().toString();
                            }
                        }
                        dataRow.createCell(j).setCellValue(value);
                        begin += JedisMaster.TimeUnit.one_day;
                    }
                }
            }
        }


//         Ghi workbook vào ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        // Tạo header cho phản hồi HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "data.xlsx");
        return CommandReportExcel.builder()
                .headers(headers)
                .bytes(outputStream.toByteArray())
                .build();
    }

}
