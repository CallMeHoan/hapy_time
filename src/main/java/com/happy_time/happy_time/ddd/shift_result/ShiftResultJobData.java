package com.happy_time.happy_time.ddd.shift_result;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ShiftResultJobData {
    private List<String> agent_ids;
    private String tenant_id;
    private ReferenceData ref_data;
    private String shift_assigned_id;
}
