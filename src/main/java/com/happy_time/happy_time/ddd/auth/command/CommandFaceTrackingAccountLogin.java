package com.happy_time.happy_time.ddd.auth.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandFaceTrackingAccountLogin {
    private String user_name;
    private String password;
}
