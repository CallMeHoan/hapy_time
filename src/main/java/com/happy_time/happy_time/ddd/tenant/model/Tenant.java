package com.happy_time.happy_time.ddd.tenant.model;

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
@Document(collection = "tenants")
public class Tenant implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private Long created_date;
    private Long last_updated_date;
    private String company_name; // tên công ty
    @Builder.Default
    private Boolean status = true;
    private Long scale; //total member of company
    @Builder.Default
    private Boolean is_deleted = false;

    private ReferenceData created_by;
    private ReferenceData last_updated_by;

    private String company_shorthand;

}
