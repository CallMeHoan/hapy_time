package com.happy_time.happy_time.ddd.shift_type.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.ddd.shift_type.ShiftType;
import com.happy_time.happy_time.ddd.shift_type.application.ShiftTypeApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/default/shift_type")
public class ShiftTypeController {
    @Autowired
    private ShiftTypeApplication shiftTypeApplication;

    @GetMapping("/get_all")
    public Optional<ResponseObject> getAll(HttpServletRequest httpServletRequest) {
        try {
            List<ShiftType> list = shiftTypeApplication.getAll();
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(list).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> create(HttpServletRequest httpServletRequest, @RequestBody ShiftType config) {
        try {
            ShiftType created_config = shiftTypeApplication.create(config);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(created_config).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }
}
