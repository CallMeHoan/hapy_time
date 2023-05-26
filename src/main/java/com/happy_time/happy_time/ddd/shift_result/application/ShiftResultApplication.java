package com.happy_time.happy_time.ddd.shift_result.application;

import com.happy_time.happy_time.Utils.JsonUtils;
import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.attendance.application.AttendanceConfigApplication;
import com.happy_time.happy_time.ddd.bssid_config.application.BssidConfigApplication;
import com.happy_time.happy_time.ddd.gps_config.application.GPSConfigApplication;
import com.happy_time.happy_time.ddd.ip_config.application.IPConfigApplication;
import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.application.JobApplication;
import com.happy_time.happy_time.ddd.job.executor.JobExecutor;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_assignment.application.ShiftAssignmentApplication;
import com.happy_time.happy_time.ddd.shift_assignment.service.ShiftAssignmentService;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.ShiftResultJobData;
import com.happy_time.happy_time.ddd.shift_result.repository.IShiftResultRepository;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.mongodb.client.result.UpdateResult;
import io.swagger.v3.oas.models.security.SecurityScheme;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShiftResultApplication {

    protected final Log logger = LogFactory.getLog(this.getClass());
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


    @Autowired
    private JobApplication jobApplication;

    @Autowired
    private ShiftAssignmentService shiftAssignmentService;

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
        List<String> agent_ids = agents.stream().map(i -> i.get_id().toHexString()).toList();


        List<ShiftResult.Shift> shifts = new ArrayList<>();
        //check xem đang sử dụng loại nào để tính ngày
        if (BooleanUtils.isTrue(config.getUse_specific_day()) && config.getDay_applied() != null) {
            if (!CollectionUtils.isEmpty(config.getDay_applied().getShifts()) && BooleanUtils.isTrue(config.getDay_applied().getUse_same_shift())) {
                for (ShiftAssignment.Shift shift : config.getDay_applied().getShifts()) {
                    ShiftResult.Shift s = ShiftResult.Shift.builder()
                            .shift_schedule_ids(shift.getShift_ids())
                            .date(shift.getDate())
                            .build();
                    shifts.add(s);
                }
                List<ShiftResult> results = new ArrayList<>();
                for (String id : agent_ids) {
                    for (ShiftResult.Shift shift : shifts) {
                        ShiftResult res = ShiftResult.builder()
                                .tenant_id(config.getTenant_id())
                                .shift_assigned_id(config.get_id().toHexString())
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
        }

        //day range
        if (BooleanUtils.isTrue(config.getUse_day_range())
                && config.getDay_range() != null
                && BooleanUtils.isTrue(config.getDay_range().getUse_same_shift())
                && config.getDay_range().getFrom() != null
                && !CollectionUtils.isEmpty(config.getDay_range().getShift_ids())) {
            //build command để set vào job -> execute
            //nếu như có ca trong ngày thì sẽ tạo ca cho ngày đó và set job cho những ngày tiếp theo
            String date_execute = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, config.getDay_range().getFrom());
            String day_in_string = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis());
            String job_execute_date = date_execute;
            if (day_in_string.equals(date_execute)) {
                // nếu như trong config ngày làm bằng ngày hiện tại => tạo shift result cho nhân viên
                ShiftResult.Shift s = ShiftResult.Shift.builder()
                        .shift_schedule_ids(config.getDay_range().getShift_ids())
                        .date(day_in_string)
                        .build();
                List<ShiftResult> results = new ArrayList<>();
                for (String id : agent_ids) {
                    ShiftResult res = ShiftResult.builder()
                            .tenant_id(config.getTenant_id())
                            .shift_assigned_id(config.get_id().toHexString())
                            .agent_id(id)
                            .create_by(config.getCreate_by())
                            .last_update_by(config.getLast_update_by())
                            .created_at(current)
                            .last_updated_at(current)
                            .shift(s)
                            .build();
                    results.add(res);
                }
                mongoTemplate.insert(results, "shift_result");

                job_execute_date = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis() + DateTimeUtils.Milisecond.DAY);
            }

            ShiftResultJobData data = ShiftResultJobData.builder()
                    .agent_ids(agent_ids)
                    .tenant_id(config.getTenant_id())
                    .ref_data(config.getCreate_by())
                    .shift_assigned_id(config.get_id().toHexString())
                    .build();
            String job_data = JsonUtils.toJSON(data);
            //build job data để add
            JobModel job = JobModel.builder()
                    .action(JobAction.set_shift_result)
                    .executed_time(job_execute_date)
                    .tenant_id(config.getTenant_id())
                    .created_at(System.currentTimeMillis())
                    .last_updated_at(System.currentTimeMillis())
                    .job_data(job_data)
                    .build();
            jobApplication.setJob(job);
        }
    }

    public ShiftResult getById(String id) {
        return mongoTemplate.findById(id, ShiftResult.class);
    }

    public ShiftResult update(ShiftResult shift_result) {
        shift_result.setLast_updated_at(System.currentTimeMillis());
        return mongoTemplate.save(shift_result, "shift_result");
    }

    public ShiftResult getByAgent(String tenant_id, String agent_id) {
        String current_day = DateTimeUtils.convertLongToDate("dd/MM/yyyy", System.currentTimeMillis());
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("agent_id").is(agent_id));
        query.addCriteria(Criteria.where("shift.date").is(current_day));
        return mongoTemplate.findOne(query, ShiftResult.class);
    }

    public Boolean deleteWhenDeleteAssignment(String tenant_id, String assignment_id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("is_deleted").is(false));
        query.addCriteria(Criteria.where("tenant_id").is(tenant_id));
        query.addCriteria(Criteria.where("shift_assigned_id").is(assignment_id));

        Update update = new Update();
        update.setOnInsert("is_deleted", true);
        mongoTemplate.updateMulti(query, update, "shift_result");
        return true;
    }

    public void executeJob(JobModel jobModel) {
        if (jobModel == null) {
            return;
        }
        if (BooleanUtils.isTrue(jobModel.getExecuted())) {
            logger.error("ShiftResultApplication job executed:" + jobModel.get_id().toHexString());
            return;
        }
        if (StringUtils.isBlank(jobModel.getTenant_id())) {
            logger.error("ShiftResultApplication missing tenant_id:" + jobModel.get_id().toHexString());
            return;
        }
        String current_date = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, System.currentTimeMillis());

        if (!current_date.equals(jobModel.getExecuted_time())) {
            logger.error("ShiftResultApplication not in executed time:" + jobModel.get_id().toHexString());
            return;
        }

        if (StringUtils.isBlank(jobModel.getJob_data())) {
            logger.error("ShiftResultApplication job data null:" + jobModel.get_id().toHexString());
            return;
        }

        ShiftResultJobData data = JsonUtils.jsonToObject(jobModel.getJob_data(), ShiftResultJobData.class);
        if (data == null) {
            logger.error("ShiftResultApplication job data null:" + jobModel.get_id().toHexString());
            return;
        }

        if (CollectionUtils.isEmpty(data.getAgent_ids())) {
            logger.error("ShiftResultApplication no agents found:" + jobModel.get_id().toHexString());
            return;
        }

        if (StringUtils.isBlank(data.getShift_assigned_id())) {
            logger.error("ShiftResultApplication missing shift assignment:" + jobModel.get_id().toHexString());
            return;
        }

        //xử lý sau khi bypass all
        ShiftAssignment config = shiftAssignmentService.getById(data.getShift_assigned_id());
        if (config == null) {
            logger.error("ShiftResultApplication no config found:" + jobModel.get_id().toHexString());
            return;
        }

        if (config.getDay_range() != null) {
            if (CollectionUtils.isEmpty(config.getDay_range().getDays())) {
                logger.error("ShiftResultApplication no days in config:" + jobModel.get_id().toHexString());
                return;
            }
            //set ca + set job cho ngày tíếp theo
            //check xem ngày hiện tại có nằm trong config không nếu có thì tạo không thì bỏ qua
            Long current = System.currentTimeMillis();
            Integer current_day = DateTimeUtils.getDayOfWeek(current);
            if (!config.getDay_range().getDays().contains(current_day)) {
                logger.info("ShiftResultApplication current day do not have in config:" + jobModel.get_id().toHexString());
                return;
            }

            String date_execute = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, current);
            if (!CollectionUtils.isEmpty(config.getDay_range().getShift_ids())) {
                ShiftResult.Shift s = ShiftResult.Shift.builder()
                        .shift_schedule_ids(config.getDay_range().getShift_ids())
                        .date(date_execute)
                        .build();
                List<ShiftResult> results = new ArrayList<>();
                for (String id : data.getAgent_ids()) {
                    ShiftResult res = ShiftResult.builder()
                            .tenant_id(config.getTenant_id())
                            .shift_assigned_id(config.get_id().toHexString())
                            .agent_id(id)
                            .create_by(config.getCreate_by())
                            .last_update_by(config.getLast_update_by())
                            .created_at(current)
                            .last_updated_at(current)
                            .shift(s)
                            .build();
                    results.add(res);
                }
                mongoTemplate.insert(results, "shift_result");
            }

            //check tới ngày ngừng thực hiện hay chưa
            if (config.getDay_range().getTo() != null && config.getDay_range().getTo() < current) {
                logger.info("ShiftResultApplication end of execution" + jobModel.get_id().toHexString());
                return;
            }

            //tính ngày thực hiện tiếp theo rồi thêm job

            String next_day = this.calculateNextExecuteTime(config.getDay_range());
            if (StringUtils.isNotBlank(next_day)) {
                //nếu thỏa rồi thì tạo job
                ShiftResultJobData new_job_data = ShiftResultJobData.builder()
                        .agent_ids(data.getAgent_ids())
                        .tenant_id(config.getTenant_id())
                        .ref_data(config.getCreate_by())
                        .shift_assigned_id(config.get_id().toHexString())
                        .build();
                String job_data = JsonUtils.toJSON(new_job_data);
                //build job data để add
                JobModel job = JobModel.builder()
                        .action(JobAction.set_shift_result)
                        .executed_time(next_day)
                        .tenant_id(config.getTenant_id())
                        .created_at(System.currentTimeMillis())
                        .last_updated_at(System.currentTimeMillis())
                        .job_data(job_data)
                        .build();
                jobApplication.setJob(job);
            } else {
                logger.info("ShiftResultApplication end of execution");
            }
        }
    }

    private String calculateNextExecuteTime(ShiftAssignment.DayRange config) {
        Long current = System.currentTimeMillis();
        String res = "";
        if (config.getFrom() < current && current < config.getTo()) {
            Long time_stamp = current;
            int retry = 0;
            while (true) {
                if (retry == 6) {
                    logger.error("calculateNextExecuteTime retry 6 time => failed");
                    break;
                }
                Long next_date = time_stamp + 86400;
                Integer day_of_week = DateTimeUtils.getDayOfWeek(next_date);
                if(config.getDays().contains(day_of_week) && config.getFrom() < next_date && next_date < config.getTo()) {
                    res = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, next_date);
                    return res;
                }
                time_stamp += 3600 * 24 * 1000;
                retry += 1;
            }
        }
        return res;
    }

}
