package com.happy_time.happy_time.ddd.configuration.ip_config.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandIPConfig {
    private String tenant_id;
    private String ip_name;
    private String status;
}