package com.happy_time.happy_time.ddd.leave_policy.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandLeavePolicy {
    private String tenant_id;
    private Integer official_agent_total_leave;
    private Long leave_cut_off;
    private String leave_type;
    private ReferenceData ref;
}
