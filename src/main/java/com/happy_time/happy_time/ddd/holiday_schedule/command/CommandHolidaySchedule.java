package com.happy_time.happy_time.ddd.holiday_schedule.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandHolidaySchedule {
    private String tenant_id;
    private String holiday_name;
    private Long date_from;
    private Long date_to;
    private ReferenceData last_updated_by;
    private ReferenceData created_by;
    private String keyword;
}
