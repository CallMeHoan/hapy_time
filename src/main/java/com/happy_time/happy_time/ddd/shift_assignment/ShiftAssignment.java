package com.happy_time.happy_time.ddd.shift_assignment;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "shift_assignment")
public class ShiftAssignment {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_at;
    private Long last_updated_at;
    private String name;
    private String name_unsigned;
    private String apply_for;
    private List<String> departments;
    private List<String> positions;
    private List<String> agents;
    @Builder.Default
    private Boolean use_day_range = true;
    @Builder.Default
    private Boolean use_specific_day = false;
    private DayRange day_range;
    private DayApplied day_applied;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class DayRange {
        private Long from;
        private Long to;
        private RepeatConfig repeat_config;
        private List<Shift> shifts;
        private Boolean use_same_shift;
        private Boolean use_separate_shift;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class RepeatConfig {
        private String repeat_time; //day, week
        private String repeat_method; //daily, weekly
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Shift{
        private String day; // thứ trong tuần
        private String date; //ngày cụ thể
        private List<String> shift_ids;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class DayApplied {
        private List<Shift> shifts;
        @Builder.Default
        private Boolean use_same_shift = true;
        @Builder.Default
        private Boolean use_separate_shift = false;
    }

}
