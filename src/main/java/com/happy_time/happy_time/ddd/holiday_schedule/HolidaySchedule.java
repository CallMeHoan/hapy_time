package com.happy_time.happy_time.ddd.holiday_schedule;

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
@Document(collection = "holiday_schedule")
public class HolidaySchedule implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id;

    @Builder.Default
    private Boolean is_deleted = false;
    private Long created_at;
    private Long last_updated_at;
    private ReferenceData created_by;
    private ReferenceData last_updated_by;
    private String holiday_name;
    private String holiday_name_unsigned;
    private Long date_from;
    private Long date_to;
}
