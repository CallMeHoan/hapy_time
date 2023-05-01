package com.happy_time.happy_time.ddd.auth.controller;

import com.google.gson.Gson;
import com.happy_time.happy_time.Utils.ResponseObject;
import com.happy_time.happy_time.constant.AppConstant;
import com.happy_time.happy_time.constant.ExceptionMessage;
import com.happy_time.happy_time.ddd.agent.application.AgentApplication;
import com.happy_time.happy_time.ddd.agent.command.CommandChangePassword;
import com.happy_time.happy_time.ddd.agent.command.CommandValidate;
import com.happy_time.happy_time.ddd.agent.model.Agent;
import com.happy_time.happy_time.ddd.auth.application.AuthApplication;
import com.happy_time.happy_time.ddd.auth.command.CommandCreatePassword;
import com.happy_time.happy_time.ddd.auth.command.CommandLogin;
import com.happy_time.happy_time.ddd.auth.command.CommandRegister;
import com.happy_time.happy_time.ddd.auth.command.CommandSendSms;
import com.happy_time.happy_time.ddd.auth.model.Account;
import com.happy_time.happy_time.ddd.auth.model.SmsModel;
import com.happy_time.happy_time.jwt.JWTUtility;
import com.happy_time.happy_time.service.UserService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {
    @Autowired
    private AuthApplication authApplication;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private AgentApplication agentApplication;

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
    public Optional<ResponseObject> sendSms(@Valid @RequestBody CommandSendSms smsRequest) {
        try {
            Random rnd = new Random();
            Integer number = rnd.nextInt(999999);
            String message = number + " la ma xac minh dang ky Baotrixemay cua ban";
            String phone_number = smsRequest.getPhone_number();
            SmsModel model = SmsModel.builder()
                    .ApiKey("A0E1219E96666C39BB85DEB131CDF0")
                    .Content(message)
                    .Phone(phone_number)
                    .SecretKey("3F280AF4E169D098A453128DFA2F98")
                    .Brandname("baotrixemay")
                    .SmsType("2")
                    .IsUnicode(0)
                    .SandBox(0)
                    .build();

            String body = new Gson().toJson(model);

            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(body, MediaType.parse("application/json; charset=utf-8"));

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_post_json/")
                    .method("POST", requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Cookie", "ASP.NET_SessionId=4zhxi2iaxcyqrlooff2u3vj1")
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.body().string().contains("100")) {
                throw new Exception("can_not_send_sms");
            }
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
    public ResponseObject authenticateUser(@Valid @RequestBody CommandLogin command) throws Exception {
        try {
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

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseObject.builder().status(9999).message("success").payload(response).build();
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", ExceptionMessage.WRONG_USERNAME_OR_PASSWORD);
            return ResponseObject.builder().status(-9999).message("failed").payload(response).build();
        }
    }


    @PostMapping("/validate")
    public Optional<ResponseObject> validate(@RequestBody CommandValidate command) {
        try {
            if (command == null) {
                throw new IllegalArgumentException(ExceptionMessage.MISSING_PARAMS);
            }
            Boolean validated = agentApplication.validatePhoneNumberAndEmail(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(validated).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/forget_password")
    public Optional<ResponseObject> forgetPassword(@RequestBody CommandChangePassword command) {
        try {
            if (command == null) {
                throw new IllegalArgumentException(ExceptionMessage.MISSING_PARAMS);
            }
            Boolean validated = authApplication.forgetPassword(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(validated).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/change_password")
    public Optional<ResponseObject> changePassword(@RequestBody CommandChangePassword command) {
        try {
            if (command == null) {
                throw new IllegalArgumentException(ExceptionMessage.MISSING_PARAMS);
            }
            Boolean validated = authApplication.changePassword(command);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(validated).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get_is_used_happy_time/{phone_number}")
    public Optional<ResponseObject> getIsUsedHappyTime(@PathVariable String phone_number) {
        try {
            Boolean is_used = authApplication.getIsUsedHappyTime(phone_number);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(is_used).build();
            return Optional.of(res);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.of(ResponseObject.builder().status(9999).message("failed").payload(e.getMessage()).build());
        }
    }

    @PostMapping("/create")
    public Optional<ResponseObject> register(@RequestBody Account account) {
        try {
            if (account == null) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            authApplication.create(account);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload("create_account_successfully").build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @GetMapping("/get_agent/{phone_number}")
    public Optional<ResponseObject> getByPhoneNumber(@PathVariable String phone_number) {
        try {
            Agent agent = authApplication.getAgentByPhoneNumber(phone_number);
            ResponseObject res = ResponseObject.builder().status(9999).message("success").payload(agent).build();
            return Optional.of(res);
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

    @PostMapping("/create/password")
    public Optional<ResponseObject> createPassword(@RequestBody CommandCreatePassword command) {
        try {
            if (StringUtils.isEmpty(command.getPhone_number()) || StringUtils.isEmpty(command.getPassword())) {
                throw new Exception(ExceptionMessage.MISSING_PARAMS);
            }
            Boolean res = authApplication.createPassword(command);
            return Optional.of(ResponseObject.builder().status(9999).message("success").payload(res).build());
        } catch (Exception e) {
            ResponseObject res = ResponseObject.builder().status(-9999).message("failed").payload(e.getMessage()).build();
            return Optional.of(res);
        }
    }

}
