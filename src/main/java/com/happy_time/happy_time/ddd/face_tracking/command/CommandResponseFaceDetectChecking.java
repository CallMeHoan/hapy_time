package com.happy_time.happy_time.ddd.face_tracking.command;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandResponseFaceDetectChecking {
    private List<String> acceptable_images;
    private List<String> unacceptable_images;
}
