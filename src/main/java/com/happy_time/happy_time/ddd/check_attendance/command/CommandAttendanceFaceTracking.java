package com.happy_time.happy_time.ddd.check_attendance.command;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandAttendanceFaceTracking {
    private String tenant_id;
    private String image;
    private String agent_id;
}
