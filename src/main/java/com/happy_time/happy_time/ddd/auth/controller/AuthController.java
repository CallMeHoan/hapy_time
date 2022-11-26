package com.happy_time.happy_time.ddd.auth.controller;

import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.auth.application.AuthApplication;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.auth.service.Service;
import com.happy_time.happy_time.jwt.JWTUtility;
import com.happy_time.happy_time.service.UserService;
import com.happy_time.happy_time.twilio.sms_request.SmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private JWTUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    public AuthController(Service service) {
        this.service = service;
    }

    @PostMapping("/register")
    public Optional<ResponseObject> register(@RequestBody CommandRegister command) {
        try {
            if (command == null) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            Account register = authApplication.register(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("create_account_successfully").build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
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
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/login")
    public Map<String, String> authenticateUser(@Valid @RequestBody CommandRegister command) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            command.getPhone_number(),
                            command.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        final Account userDetails = authApplication.findByPhoneNumber(command.getPhone_number());

        final String token = jwtUtility.generateToken(userDetails);

        Map<String, String> res = new HashMap<>();
        res.put("token", token);

        return res;
    }
}
