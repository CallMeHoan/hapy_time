package com.happy_time.happy_time.ddd.request_procedure;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.constant.AppConstant;
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
@Document(collection = "request_procedure")
public class RequestProcedure {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    @Builder.Default
    private String tenant_id = AppConstant.TENANT_DEFAULT;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_date;
    private Long last_updated_date;
    private String name;
    private String name_unsigned;
    private List<String> departments;
    private List<String> positions;
    private List<String> agents;
    private List<Stage> stages;
    private List<String> request_ids; // loại đơn áp dụng
    private Follows follows;
    private String type; //company, department, position, agent

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Stage {
        @Builder.Default
        private Boolean is_need_all = true;
        @Builder.Default
        private Boolean is_main_manager = false;
        private List<String> positions_ids;
        private List<String> agent_ids;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Follows {
        private List<String> positions_ids;
        private List<String> agent_ids;
    }

}
