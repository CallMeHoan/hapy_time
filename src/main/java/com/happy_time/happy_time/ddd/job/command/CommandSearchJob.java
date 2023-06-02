package com.happy_time.happy_time.ddd.job.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandSearchJob {
    private String executed_date;
    private Long executed_time;
    private String action;
}
