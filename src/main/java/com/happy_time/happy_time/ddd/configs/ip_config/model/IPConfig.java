package com.happy_time.happy_time.ddd.configs.ip_config.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.common.Status;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "ip_config")
public class IPConfig implements Serializable {
    @Id@JsonSerialize(using = ToStringSerializer.class)
    Object _id;

    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private String ip_name;
    private String ip_address;
    private Status status;
    private Long created_date;
    private Long last_updated_date;
}
