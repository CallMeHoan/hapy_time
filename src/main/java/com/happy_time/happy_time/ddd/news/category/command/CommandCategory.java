package com.happy_time.happy_time.ddd.news.category.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandCategory {
    private String tenant_id;
    private String name;
    private Integer total_news;
    private ReferenceData ref;
}
