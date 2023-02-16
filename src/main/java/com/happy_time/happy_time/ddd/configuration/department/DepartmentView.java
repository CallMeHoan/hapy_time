package com.happy_time.happy_time.ddd.configuration.department;

import com.happy_time.happy_time.ddd.configuration.position.Position;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DepartmentView {
    private Department department;
    private List<Position> children_position;
}
