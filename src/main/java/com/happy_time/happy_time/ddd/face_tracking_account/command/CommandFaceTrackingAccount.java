package com.happy_time.happy_time.ddd.face_tracking_account.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandFaceTrackingAccount {
    private ReferenceData ref;
    private String tenant_id;
    private String user_name;
    private String password;
}
