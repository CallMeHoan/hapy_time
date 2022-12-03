package com.happy_time.happy_time.ddd.agent.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandChangePassword {
    private String new_password;
    private String phone_number;
    private String old_password;
    private String tenant_id;
    private String agent_id;
}
