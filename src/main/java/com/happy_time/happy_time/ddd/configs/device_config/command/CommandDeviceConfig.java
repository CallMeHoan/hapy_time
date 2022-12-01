package com.happy_time.happy_time.ddd.configs.device_config.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandDeviceConfig {
    private String tenant_id;
    private String ip_name;
    private String status;
}
