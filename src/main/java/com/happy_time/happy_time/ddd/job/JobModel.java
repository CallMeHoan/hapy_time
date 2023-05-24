package com.happy_time.happy_time.ddd.job;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@Document(collection = "job")
public class JobModel implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id;
    private String action; // case thực thi
    private String executed_time;
    private String job_data; //json sẽ được parse ra theo class
    @Builder.Default
    private Boolean executed = false;
    @Builder.Default
    private Boolean is_deleted = false;
    private Long created_at;
    private Long last_updated_at;
}
