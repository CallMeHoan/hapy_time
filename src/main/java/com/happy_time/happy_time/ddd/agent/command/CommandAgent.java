package com.happy_time.happy_time.ddd.agent.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandAgent {
    private String id;
    private Boolean is_used_happy_time;
    private Boolean is_has_account;
}
