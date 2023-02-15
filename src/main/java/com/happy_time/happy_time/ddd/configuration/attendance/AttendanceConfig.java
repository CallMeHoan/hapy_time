package com.happy_time.happy_time.ddd.configuration.attendance;

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
@Document(collection = "attendance_config")
public class AttendanceConfig implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id; //nếu là OMI default thì là config chung

    @Builder.Default
    private Boolean is_deleted = false;
    private Long created_at;
    private Long last_updated_at;
    private ReferenceData created_by;
    private ReferenceData last_updated_by;
    private List<Module> modules;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Module {
        private Boolean is_enabled = false;
        private List<Function> functions;
        private String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Function {
        private String name;
        private Boolean is_enabled = false;
    }
}
