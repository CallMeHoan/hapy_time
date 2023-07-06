package com.happy_time.happy_time.ddd.face_tracking.command;

import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandFaceTracking {
    private String tenant_id;
    private String agent_id;
    private ReferenceData ref;
    private List<String> face_tracking_images;
}
