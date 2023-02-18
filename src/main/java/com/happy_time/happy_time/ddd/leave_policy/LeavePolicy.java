package com.happy_time.happy_time.ddd.leave_policy;

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
@Document(collection = "leave_policy")
public class LeavePolicy implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    private String tenant_id = AppConstant.TENANT_DEFAULT;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_date;
    private Long last_updated_date;
    private Integer official_agent_total_leave;
    @Builder.Default
    private Integer intern_agent_total_leave = 0;
    private Long leave_cut_off;
    private String leave_type; // monthly / yearly
}
