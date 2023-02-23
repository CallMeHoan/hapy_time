package com.happy_time.happy_time.ddd.shift_result.application;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.repository.IShiftResultRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

}
