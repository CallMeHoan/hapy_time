package com.happy_time.happy_time.ddd.shift_result;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandSearchShiftResult {
    private String tenant_id;
    private Integer page;
    private Integer size;
    private Long from;
    private Long to;
}
