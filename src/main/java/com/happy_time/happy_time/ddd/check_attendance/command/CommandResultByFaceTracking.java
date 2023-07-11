package com.happy_time.happy_time.ddd.check_attendance.command;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandResultByFaceTracking {
    private Long attendance_time;
    private String type; //check_in || check_out
    private String agent_name;
    private String department_name;
}
