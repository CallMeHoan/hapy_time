package com.happy_time.happy_time.ddd.configs.calendar_config.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandCalendarConfig {
    private String tenant_id;
    private Boolean is_active;
    private String calendar_code;
    private String calendar_name;
}
