package com.happy_time.happy_time.ddd.configuration.department.command;

import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.ddd.configuration.position.Position;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandDepartment {
    private String tenant_id;
    private String name;
    private String department_parent_id;
    private List<Position> positions;
    private ReferenceData created_by;
    private ReferenceData last_updated_by;
}
