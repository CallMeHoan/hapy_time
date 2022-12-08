package com.happy_time.happy_time.ddd.auth.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandSendSms {
    private String phone_number;
}
