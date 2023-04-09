package com.happy_time.happy_time.ddd.device.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandDevice {
    private String tenant_id;
    private String agent_id;
    private String device_name;
    private String device_id;
    private ReferenceData ref;
    private Boolean status;
}
