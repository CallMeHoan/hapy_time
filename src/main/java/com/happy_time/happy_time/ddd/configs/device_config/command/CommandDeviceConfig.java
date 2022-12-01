package com.happy_time.happy_time.ddd.configs.device_config.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandDeviceConfig {
    private String tenant_id;
    private String department;
    private String device_id;
    private String agent_code;
    private String agent_name;
    private String status;
}
