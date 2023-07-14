package com.happy_time.happy_time.ddd.attendance_report.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandAttendanceReport {
    private String tenant_id;
    private String agent_id;
    private ReferenceData ref;
    private Long created_date;
    private Long last_updated_date;
    private Double total_work_count;
    private Double current_work_count;
    private Long late;
    private Long soon;
    private String month;
}
