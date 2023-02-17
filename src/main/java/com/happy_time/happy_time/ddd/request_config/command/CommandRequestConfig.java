package com.happy_time.happy_time.ddd.request_config.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandRequestConfig {
    private String tenant_id;
    private String procedure;
    private ReferenceData ref;
    private Boolean is_in_use;
}
