package com.happy_time.happy_time.ddd.agent.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AgentView {
    private String id;
    private String name;
    private String avatar;
    private String position;
}
