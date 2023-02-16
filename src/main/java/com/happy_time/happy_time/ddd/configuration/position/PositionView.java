package com.happy_time.happy_time.ddd.configuration.position;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PositionView {
    private String id;
    private String position_name;
    private Boolean is_manager;
    private String department_id; //id của phòng ban
    private String position_code;
}
