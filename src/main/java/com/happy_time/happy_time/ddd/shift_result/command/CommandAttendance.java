package com.happy_time.happy_time.ddd.shift_result.command;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandAttendance {
    private String tenant_id;
    private String agent_id;
    private String device_id;
    private String ip_address;
    private String bssid_address;
    private String shift_id;
    private Double lat; //kinh độ
    private Double lon; // vĩ độ
    private String type; // check_in, check_out
}
