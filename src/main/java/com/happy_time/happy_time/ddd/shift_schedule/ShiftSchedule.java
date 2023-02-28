package com.happy_time.happy_time.ddd.shift_schedule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.ddd.shift_type.ShiftTypeView;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "shift_schedule")
public class ShiftSchedule {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_date;
    private Long last_updated_date;
    private String name;
    private String name_unsigned;
    private ShiftTypeView shift_type;
    private String code;
    @Builder.Default
    private Boolean is_enabled = true;
    private Double work_count; //full công
    private Double partial_work_count; // công nếu không checkout
    @Builder.Default
    private Boolean is_using_check_in_limit = true;
    @Builder.Default
    private Boolean is_using_check_out_limit = true;
    private TimeAdvanceConfig config_in_late;
    private TimeAdvanceConfig config_out_early;

    //cho ca đơn
    private TimeRange working_time; //giờ làm việc
    private TimeRange allow_in_time; //giờ check in hợp lệ
    private TimeRange allow_out_time; // giờ checkout hợp lệ

    //ca hành chính
    private TimeRange afternoon_allow_in_time;
    private TimeRange afternoon_allow_out_time;
    private TimeRange afternoon_working_time;
    private TimeRange morning_allow_in_time;
    private TimeRange morning_allow_out_time;
    private TimeRange morning_working_time;


    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class TimeRange {
        private String from;
        private String to;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class TimeAdvanceConfig {
        private Boolean is_in_use;
        private String time; // cho ca đơn
        private String late_in_morning;
        private String early_out_morning;
        private String late_in_afternoon;
        private String early_out_afternoon;
    }


}
