package com.happy_time.happy_time.ddd.shift_result.application;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.attendance.application.AttendanceConfigApplication;
import com.happy_time.happy_time.ddd.bssid_config.application.BssidConfigApplication;
import com.happy_time.happy_time.ddd.gps_config.application.GPSConfigApplication;
import com.happy_time.happy_time.ddd.ip_config.application.IPConfigApplication;
import com.happy_time.happy_time.ddd.job.JobModel;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.repository.IShiftResultRepository;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
        List<String> agent_ids = agents.stream().map(i -> i.get_id().toHexString()).toList();


        List<ShiftResult.Shift> shifts = new ArrayList<>();
        //check xem đang sử dụng loại nào để tính ngày
        if (BooleanUtils.isTrue(config.getUse_specific_day()) && config.getDay_applied() != null) {
            if (!CollectionUtils.isEmpty(config.getDay_applied().getShifts())) {
                for (ShiftAssignment.Shift shift : config.getDay_applied().getShifts()) {
                    ShiftResult.Shift s = ShiftResult.Shift.builder()
                            .shift_schedule_ids(shift.getShift_ids())
                            .date(shift.getDate())
                            .build();
                    shifts.add(s);
                }
            }
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

    public void executeJob(JobModel jobModel){

    }



}
