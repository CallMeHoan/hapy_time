package com.happy_time.happy_time.ddd.configs.calendar_config.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "calendar_config")
public class CalendarConfig implements Serializable {
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
    private String calendar_code;
    private Boolean is_active;
    private Time start_working_time;
    private Time end_working_time;
    private TimeRange check_in_time;
    private TimeRange check_out_time;
    private Double total_recognition; //Số công ghi nhận nếu có checkin + checkout đầy đủ
    private Double total_when_forget_check_out; // Số công ghi nhân nếu quên checkout
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Time {
        private Integer hour;
        private Integer minute;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class TimeRange {
        private Time from;
        private Time to;
    }


}
