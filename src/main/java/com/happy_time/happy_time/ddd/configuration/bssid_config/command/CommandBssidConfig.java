package com.happy_time.happy_time.ddd.configuration.bssid_config.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandBssidConfig {
    private String tenant_id;
    private String bssid_name;
    private String bssid_address;
    private ReferenceData last_updated_by;
    private ReferenceData created_by;
}
