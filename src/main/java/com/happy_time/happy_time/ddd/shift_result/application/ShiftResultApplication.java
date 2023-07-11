package com.happy_time.happy_time.ddd.shift_result.application;

import com.happy_time.happy_time.Utils.JsonUtils;
import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.command.CommandSearchAgent;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.agent.model.AgentView;
import com.happy_time.happy_time.ddd.attendance.application.AttendanceConfigApplication;
import com.happy_time.happy_time.ddd.bssid_config.application.BssidConfigApplication;
import com.happy_time.happy_time.ddd.gps_config.application.GPSConfigApplication;
import com.happy_time.happy_time.ddd.ip_config.application.IPConfigApplication;
import com.happy_time.happy_time.ddd.job.JobAction;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.job.application.JobApplication;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_assignment.service.ShiftAssignmentService;
import com.happy_time.happy_time.ddd.shift_result.CommandSearchShiftResult;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.ShiftResultJobData;
import com.happy_time.happy_time.ddd.shift_result.ShiftResultView;
import com.happy_time.happy_time.ddd.shift_result.repository.IShiftResultRepository;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.happy_time.happy_time.ddd.shift_schedule.application.ShiftScheduleApplication;
import com.happy_time.happy_time.ddd.shift_type.ShiftType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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
    private ShiftScheduleApplication shiftScheduleApplication;

    @Autowired
    private JobApplication jobApplication;

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
            if (BooleanUtils.isTrue(config.getDay_applied().getUse_same_shift())
                    && !CollectionUtils.isEmpty(config.getDay_applied().getShift_ids())
                    && !CollectionUtils.isEmpty(config.getDay_applied().getDates())) {
                for (String date : config.getDay_applied().getDates()) {
                    ShiftResult.Shift s = ShiftResult.Shift.builder()
                            .shift_schedule_ids(config.getDay_applied().getShift_ids())
                            .date(date)
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

    public Page<ShiftResultView> getByTenant(CommandSearchShiftResult command) throws Exception {
        List<ShiftResultView> res = new ArrayList<>();
        Pageable pageRequest = PageRequest.of(command.getPage(), command.getSize());
        CommandSearchAgent commandSearchAgent = CommandSearchAgent.builder()
                .tenant_id(command.getTenant_id())
                .build();
        Page<Agent> agent_items = agentApplication.search(commandSearchAgent, command.getPage(), command.getSize());
        if (agent_items != null && agent_items.getSize() > 0) {
            List<Agent> agents = agent_items.getContent();
            for (Agent agent : agents) {
                Query query = new Query();
                query.addCriteria(Criteria.where("is_deleted").is(false));
                query.addCriteria(Criteria.where("tenant_id").is(command.getTenant_id()));
                query.addCriteria(Criteria.where("agent_id").is(agent.get_id().toHexString()));
                if (command.getFrom() != null && command.getTo() != null) {
                    query.addCriteria(Criteria.where("created_at").gte(command.getFrom()).lte(command.getTo()));
                }
                AgentView agent_view = agentApplication.setView(agent.get_id().toHexString(), agent.getTenant_id());
                List<ShiftResultView.ShiftByDate> shifts = new ArrayList<>();
                ShiftResultView view = ShiftResultView.builder()
                        .tenant_id(agent.getTenant_id())
                        .agent_name(agent_view.getName())
                        .agent_id(agent_view.getId())
                        .avatar(agent_view.getAvatar())
                        .position(agent_view.getPosition())
                        .build();
                List<ShiftResult> items = mongoTemplate.find(query, ShiftResult.class);
                if (!CollectionUtils.isEmpty(items)) {
                    for (ShiftResult item : items) {
                        ShiftSchedule shift_assigned = shiftScheduleApplication.getById(item.getShift().getShift_schedule_ids().get(0));
                        if (shift_assigned != null) {
                            String end = "";
                            String start = "";
                            if (shift_assigned.getMorning_working_time() != null && shift_assigned.getAfternoon_working_time() != null) {
                                end = shift_assigned.getAfternoon_working_time().getTo();
                                start = shift_assigned.getMorning_working_time().getFrom();

                            } else if (shift_assigned.getWorking_time() != null) {
                                end = shift_assigned.getWorking_time().getTo();
                                start = shift_assigned.getWorking_time().getFrom();
                            }
                            ShiftResultView.ShiftByDate shift = ShiftResultView.ShiftByDate.builder()
                                    .date(item.getShift().getDate())
                                    .end(end)
                                    .start(start)
                                    .shift_schedule_id(shift_assigned.get_id().toHexString())
                                    .shift_name(shift_assigned.getName())
                                    .shift_code(shift_assigned.getCode())
                                    .shift_type(shift_assigned.getShift_type())
                                    .build();
                            shifts.add(shift);
                        }
                    }
                }
                view.setShifts_by_date(shifts);
                res.add(view);
            }
            return PageableExecutionUtils.getPage(
                    res,
                    pageRequest,
                    agent_items::getTotalElements);
        }
        return null;
    }

    public void create(List<ShiftResult> shiftResult) {
        mongoTemplate.insert(shiftResult, "shift_result");
    }
}
