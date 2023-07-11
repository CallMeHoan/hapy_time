package com.happy_time.happy_time.ddd.check_attendance.command;

import lombok.*;
import org.springframework.http.HttpHeaders;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandReportExcel {
    private HttpHeaders headers;
    private byte[] bytes;

}
