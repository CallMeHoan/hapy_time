package com.happy_time.happy_time.ddd.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SmsModel {
    private String ApiKey;
    private String Content;
    private String Phone;
    private String SecretKey;
    private String Brandname;
    private String SmsType;
    private Integer IsUnicode;
    private Integer SandBox;
}
