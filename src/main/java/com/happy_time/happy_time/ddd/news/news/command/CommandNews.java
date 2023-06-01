package com.happy_time.happy_time.ddd.news.news.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandNews {
    private String tenant_id;
    private String title;
    private String content;
    private String banner;
    private String status;
    private Long post_date;
    private ReferenceData ref;
    private String category_id;
    private Integer total_replies;
    private Integer total_likes;
}
