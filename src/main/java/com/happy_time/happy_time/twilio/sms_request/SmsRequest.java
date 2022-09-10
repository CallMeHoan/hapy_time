package com.happy_time.happy_time.twilio.sms_request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SmsRequest {
    private String phone_number;
    private String message;
}