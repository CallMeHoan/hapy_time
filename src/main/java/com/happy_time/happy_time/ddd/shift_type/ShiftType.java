package com.happy_time.happy_time.ddd.shift_type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
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
@Document(collection = "shift_type")
public class ShiftType {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    @Builder.Default
    private String tenant_id = AppConstant.TENANT_DEFAULT;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private String schedule_name;
    private String description;
    private Long created_date;
    private Long last_updated_date;
}
