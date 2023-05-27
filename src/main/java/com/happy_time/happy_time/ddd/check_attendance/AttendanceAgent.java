package com.happy_time.happy_time.ddd.check_attendance;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AttendanceAgent {
    private String agent_id;
    private String tenant_id;
    private String agent_name;
    private String gender;
    private String avatar;
    private List<CheckAttendanceResult> check_attendance_results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class CheckAttendanceResult {
        private String attendance_date;
        private Long checked_in_at;
        private Long checked_out_at;
        private Double work_count;
        private Boolean is_late;
        private Boolean is_check_out_soon;
    }

}
