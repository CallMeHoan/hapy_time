package com.happy_time.happy_time.ddd.auth.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandLogin {
    private String phone_number;
    private String password;
}
