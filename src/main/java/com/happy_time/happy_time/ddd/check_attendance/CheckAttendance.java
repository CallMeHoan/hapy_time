package com.happy_time.happy_time.ddd.check_attendance;

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
@Document(collection = "check_attendance")
public class CheckAttendance  implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    private String tenant_id;
    private String agent_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_date;
    private Long last_updated_date;
    private Long checked_in_at;
    private Long checked_out_at;
    private Double work_count;
    private String attendance_date;
    private Integer position; //for early
    private Integer up; //check up bao nhieu
    private Integer down; //check down bao nhieu
    @Builder.Default
    private Boolean is_late = false;
    @Builder.Default
    private Boolean is_check_out_soon = false;
}
