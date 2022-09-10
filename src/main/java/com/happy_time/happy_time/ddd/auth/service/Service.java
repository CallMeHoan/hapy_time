package com.happy_time.happy_time.ddd.auth.service;

import com.happy_time.happy_time.twilio.TwilioSmsSender;
import com.happy_time.happy_time.twilio.sms_request.SmsRequest;
import com.happy_time.happy_time.twilio.sms_sender.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@org.springframework.stereotype.Service
public class Service {

    private final SmsSender smsSender;

    @Autowired
    public Service(@Qualifier("twilio") TwilioSmsSender smsSender) {
        this.smsSender = smsSender;
    }

    public void sendSms(SmsRequest smsRequest) {
        smsSender.sendSms(smsRequest);
    }
}