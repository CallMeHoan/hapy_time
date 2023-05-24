package com.happy_time.happy_time.ddd.shift_assignment.service;

import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import com.happy_time.happy_time.ddd.shift_assignment.application.ShiftAssignmentApplication;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShiftAssignmentService {
    private ShiftAssignmentApplication shiftAssignmentApplication;

    public ShiftAssignment getById(String id) {
        return shiftAssignmentApplication.getById(new ObjectId(id));
    }
}
