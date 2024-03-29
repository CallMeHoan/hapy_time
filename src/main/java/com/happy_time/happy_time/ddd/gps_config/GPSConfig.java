package com.happy_time.happy_time.ddd.gps_config;

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
@Document(collection = "gps_config")
public class GPSConfig implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private String gps_name;
    private String gps_name_unsigned;
    private String address;
    private String address_unsigned;
    private Double lat; //kinh độ
    private Double lon; // vĩ độ
    private Double radius; //bán kính
    private Long last_updated_date;
    private Long created_date;
}
