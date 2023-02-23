package com.happy_time.happy_time.ddd.shift_result;

import com.happy_time.happy_time.ddd.shift_type.ShiftTypeView;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ShiftResultView {
    private String tenant_id;
    private String agent_id;
    private String agent_name;
    private String avatar;
    private List<ShiftByDate> shifts_by_date;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class ShiftByDate {
        private String date;
        private String end;
        private String start;
        private String shift_schedule_id;
        private String shift_code;
        private String shift_name;
        private ShiftTypeView shift_type;
    }

}
