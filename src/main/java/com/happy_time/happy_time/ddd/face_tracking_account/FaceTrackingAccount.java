package com.happy_time.happy_time.ddd.face_tracking_account;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "face_tracking_account")
public class FaceTrackingAccount {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long last_updated_date;
    private Long created_date;
    private String user_name;
    private String password;
}
