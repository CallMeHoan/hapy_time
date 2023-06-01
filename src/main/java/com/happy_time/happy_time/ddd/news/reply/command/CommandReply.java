package com.happy_time.happy_time.ddd.news.reply.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandReply {
    private String tenant_id;
    private String reply_content;
    private String type;
    private String agent_id;
}
