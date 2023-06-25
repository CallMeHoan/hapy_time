package com.happy_time.happy_time.ddd.agent.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandSearchAgent {
    private String tenant_id;
    private String agent_id;
    private Integer agent_status;
    private String name;
    private String phone_number;
    private Integer role;
    private String agent_position;
    private Boolean is_used_happy_time;
    private String personal_mail;
    private String company_mail;
    private String agent_code;
    private Long start_working_date;
    private Long stop_working_date;
    private Long official_working_date;
}
