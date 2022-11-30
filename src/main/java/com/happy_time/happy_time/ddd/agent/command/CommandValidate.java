package com.happy_time.happy_time.ddd.agent.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandValidate {
    private String email;
    private String phone_number;
}
