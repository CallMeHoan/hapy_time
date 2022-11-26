package com.happy_time.happy_time.ddd.time_keeping.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "time_keeping")
public class TimeKeeping implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private List<Config> configs;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_date;
    private Long last_updated_date;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Config {
        private String type;
        private String format; //nếu type là phone thì cho chọn hình thức
        private Boolean is_enable; //xem xem thử có áp dụng hình thức này hay không

    }

}
