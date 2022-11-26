package com.happy_time.happy_time.common;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReferenceData {
    private String name;
    private String agent_id;
    private Long updated_at;
    private String action;
}