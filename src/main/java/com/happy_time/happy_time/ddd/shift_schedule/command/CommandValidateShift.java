package com.happy_time.happy_time.ddd.shift_schedule.command;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandValidateShift {
    private String tenant_id;
    private List<String> shift_ids;
}
