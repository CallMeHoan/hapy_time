package com.happy_time.happy_time.ddd.shift_schedule.command;

import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.ddd.shift_schedule.ShiftSchedule;
import com.happy_time.happy_time.ddd.shift_type.ShiftTypeView;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandShiftSchedule {
    private String keyword;
    private String tenant_id;
    private Boolean is_enabled = true;
    private String name;
    private String code;
    private ShiftTypeView shift_type;
    private Double work_count; //full công
    private Double partial_work_count; // công nếu không checkout
    private Boolean is_using_check_in_limit;
    private Boolean is_using_check_out_limit;
    private ShiftSchedule.TimeAdvanceConfig config_in_late;
    private ShiftSchedule.TimeAdvanceConfig config_out_early;

    //cho ca đơn
    private ShiftSchedule.TimeRange working_time; //giờ làm việc
    private ShiftSchedule.TimeRange allow_in_time; //giờ check in hợp lệ
    private ShiftSchedule.TimeRange allow_out_time; // giờ checkout hợp lệ

    //ca hành chính
    private ShiftSchedule.TimeRange afternoon_allow_in_time;
    private ShiftSchedule.TimeRange afternoon_allow_out_time;
    private ShiftSchedule.TimeRange afternoon_working_time;
    private ShiftSchedule.TimeRange morning_allow_in_time;
    private ShiftSchedule.TimeRange morning_allow_out_time;
    private ShiftSchedule.TimeRange morning_working_time;

    private ReferenceData last_updated_by;
    private String shift_type_name;

}
