package com.happy_time.happy_time.ddd.department.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
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
@Document(collection = "department")
public class Department {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id;
    private Long created_date;
    private Long last_updated_date;
    private ReferenceData created_by;
    private ReferenceData last_updated_by;
    @Builder.Default
    private Boolean is_deleted = false;
    private String department_name;
    private Integer total_members;
}
