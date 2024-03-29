package com.happy_time.happy_time.ddd.agent.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AgentV0 {
    private String _id;
    private String name;
    private Integer role;
    private String phone_number;
    private String personal_mail;
    private Boolean is_used_happy_time;
    private String department_name;
    private String position_name;
    private String avatar;
    private Integer agent_status;
    private String agent_code;
    private Long start_working_date;
    private Agent.LastLoginInfo last_login_info;
    private Integer agent_type;
    private String device_id;
    private String company_name;
    private String gender;
}
