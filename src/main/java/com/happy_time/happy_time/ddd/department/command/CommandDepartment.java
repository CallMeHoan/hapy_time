package com.happy_time.happy_time.ddd.department.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandDepartment {
    private String tenant_id;
    private String department_name;
}
