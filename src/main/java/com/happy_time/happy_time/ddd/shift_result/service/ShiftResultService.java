package com.happy_time.happy_time.ddd.shift_result.service;

import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShiftResultService {
    @Autowired
    private ShiftResultApplication shiftResultApplication;

    public ShiftResult getById(String id) {
        return shiftResultApplication.getById(id);
    }

    public void assignForAgents(ShiftAssignment shiftAssignment) throws Exception {
        shiftResultApplication.assignForAgents(shiftAssignment);
    }

    public Boolean deleteWhenDeleteAssignment(String tenant_id, String assigned_id) {
        return shiftResultApplication.deleteWhenDeleteAssignment(tenant_id, assigned_id);
    }

    public void create(List<ShiftResult> shiftResult) {
        shiftResultApplication.create(shiftResult);
    }
}
