package com.happy_time.happy_time.ddd.auth.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.ddd.auth.application.AuthApplication;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.auth.service.Service;
import com.happy_time.happy_time.twilio.sms_request.SmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    private final Service service;
    @Autowired
    private AuthApplication authApplication;

    @Autowired
    public AuthController(Service service) {
        this.service = service;
    }

    @PostMapping("/register")
    public Optional<ResponseObject> register(@RequestBody CommandRegister command) {
        try {
            if(command == null) {
                throw new IllegalArgumentException("request_body_cant_be_null");
            }
            Boolean register = authApplication.register(command);
            if(register) {
                ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("create_account_successfully").build();
                return Optional.of(res);
            } else {
                ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload("create_account_failed").build();
                return Optional.of(res);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @PostMapping("/send_otp")
    public Optional<ResponseObject> sendSms(@Valid @RequestBody SmsRequest smsRequest) {
        try {
            Random rnd = new Random();
            Integer number = rnd.nextInt(999999);
            String message = "Your Happy Time verification code is: " + number;
            String phone_number = smsRequest.getPhone_number();
            if(phone_number.startsWith("0")) {
                phone_number = phone_number.replaceFirst("0", "+84");
                smsRequest.setPhone_number(phone_number);
            }
            smsRequest.setMessage(message);
            service.sendSms(smsRequest);
            Map<String, Integer> otp_code = new HashMap<>();
            otp_code.put("otp_code", number);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(otp_code).build();
            return Optional.of(res);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }
}
