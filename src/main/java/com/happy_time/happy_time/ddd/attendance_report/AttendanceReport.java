package com.happy_time.happy_time.ddd.attendance_report;

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
@Document(collection = "attendance_report")
public class AttendanceReport implements Serializable {
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
    private Double total_work_count;
    private Double current_work_count;
    private Long late;
    private Long soon;
    private String month; //MM-yyyy này để search
}
