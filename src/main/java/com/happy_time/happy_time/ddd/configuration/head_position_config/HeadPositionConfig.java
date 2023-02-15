package com.happy_time.happy_time.ddd.configuration.head_position_config;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
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
@Document(collection = "head_position_config")
public class HeadPositionConfig implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    @Builder.Default
    private String tenant_id = AppConstant.TENANT_DEFAULT;
    private String position;
    private Long created_at;
    private Long last_updated_at;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData created_by;
    private ReferenceData last_updated_by; // cho trang admin
}
