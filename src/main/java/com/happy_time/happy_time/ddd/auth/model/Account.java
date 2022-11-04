package com.happy_time.happy_time.ddd.auth.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@Document(collection = "accounts")
public class Account {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id;
    private String agent_id;
    private String phone_number; // dùng để đăng nhập
    private String password;
    private String user_name;
    private String status;
    private String role;
    private Boolean is_deleted;
    private Long created_date;
    private Long last_updated_date;
}
