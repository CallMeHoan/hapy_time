package com.happy_time.happy_time.ddd.agent.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AgentV0 {
    private String id;
    private String name;
    private String role;
    private String phone_number;
    private String personal_mail;
    private Integer working_status;
    private Boolean is_used_happy_time;
    private String department;
    private String avatar;
    private Integer agent_status;
}
