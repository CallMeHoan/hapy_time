package com.happy_time.happy_time.ddd.check_attendance.command;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandGetAttendance {
    private String tenant_id;
    private String agent_id;
    private Long from;
    private Long to;
    private Integer page;
    private Integer size;
}
