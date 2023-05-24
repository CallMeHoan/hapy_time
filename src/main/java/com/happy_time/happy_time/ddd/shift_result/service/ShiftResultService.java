package com.happy_time.happy_time.ddd.shift_result.service;

import com.happy_time.happy_time.ddd.shift_result.ShiftResult;
import com.happy_time.happy_time.ddd.shift_result.application.ShiftResultApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShiftResultService {
    @Autowired
    private ShiftResultApplication shiftResultApplication;

    public ShiftResult getById(String id) {
        return shiftResultApplication.getById(id);
    }
}
