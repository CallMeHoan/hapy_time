package com.happy_time.happy_time.ddd.auth.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandRegister {
    private String name;
    private String phone_number;
    private String company_name;
    private String job_position;
    private String email;
    private Long scale;
    private String code;
    private String password;
    private String company_shorthand;
}
