package com.happy_time.happy_time.ddd.configs.device_config.model;

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
@Document(collection = "device_config")
public class DeviceConfig implements Serializable {
    @Id@JsonSerialize(using = ToStringSerializer.class)
    Object _id;

    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private String agent_code;
    private String agent_name;
    private String department;
    private String device_id;
    private String device_name;
    private Long created_date;
    private Long last_updated_date;
    private Status status;
}
