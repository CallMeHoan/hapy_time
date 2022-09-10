package com.happy_time.happy_time.twilio.sms_sender;

import com.happy_time.happy_time.twilio.sms_request.SmsRequest;

public interface SmsSender {
    void sendSms(SmsRequest smsRequest);
}
