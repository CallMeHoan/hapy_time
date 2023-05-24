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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                && config.getDay_range().getFrom() != null) {
            //build command để set vào job -> execute
            String date_execute = DateTimeUtils.convertLongToDate(DateTimeUtils.DATE, config.getDay_range().getFrom());

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
                    .executed_time(date_execute)
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
            logger.error("ShiftResultApplication no config found    :" + jobModel.get_id().toHexString());
            return;
        }
    }


}
