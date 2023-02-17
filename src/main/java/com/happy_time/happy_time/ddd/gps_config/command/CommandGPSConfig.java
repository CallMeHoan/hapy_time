package com.happy_time.happy_time.ddd.gps_config.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandGPSConfig {
    private String tenant_id;
    private String address;
    private Double lat;
    private Double lon;
    private Double radius;
    private String gps_name;
    private ReferenceData last_updated_by;
    private ReferenceData created_by;
    private String keyword;
}
