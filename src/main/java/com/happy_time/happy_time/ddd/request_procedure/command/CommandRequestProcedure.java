package com.happy_time.happy_time.ddd.request_procedure.command;

import com.happy_time.happy_time.common.ReferenceData;
import com.happy_time.happy_time.ddd.request_procedure.RequestProcedure;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandRequestProcedure {
    private String tenant_id;
    private ReferenceData last_updated_by;
    private ReferenceData created_by;
    private String keyword;
    private String type;
    private String name;
    private List<String> departments;
    private List<String> positions;
    private List<String> agents;
    private List<RequestProcedure.Stage> stages;
    private List<String> request_ids; // loại đơn áp dụng
    private RequestProcedure.Follows follows;
}
