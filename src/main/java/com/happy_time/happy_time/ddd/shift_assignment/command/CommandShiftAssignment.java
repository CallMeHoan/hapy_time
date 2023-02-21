package com.happy_time.happy_time.ddd.shift_assignment.command;

import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.ddd.shift_assignment.ShiftAssignment;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandShiftAssignment {
    private String tenant_id;
    private String keyword;
    private String name;
    private ReferenceData last_update_by;
}
