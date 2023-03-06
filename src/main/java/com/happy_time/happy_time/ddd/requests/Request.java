package com.happy_time.happy_time.ddd.requests;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "requests")
public class Request {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_at;
    private Long last_updated_at;
    private String code;
    private Long apply_time;
    private Double work_count;
    private String status;
    private String reason;
    private List<String> approved_agent_ids;
    private List<String> followers;
    private String request_config_id;
    private String request_config_name;
    private String tenant_name;
    private String agent_id;
    private String agent_name;
    private String gender;
    private String avatar;

}
