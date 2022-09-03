package com.happy_time.happy_time.entities.agent.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandSearchAgent {
    private String agent_status;
    private String user_name;
    private String phone_number;
    private String role;
    private String agent_position;
    private Boolean is_used_happy_time;
    private String personal_mail;
    private String company_mail;
    private String agent_code;
    private Long start_working_date;
    private Long stop_working_date;
    private Long official_working_date;
}
