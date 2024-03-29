package com.happy_time.happy_time.ddd.agent.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.happy_time.happy_time.common.ReferenceData;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "agents")
public class Agent implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;  //k cần
    private String tenant_id; //k cần
    private String avatar;
    private String name;
    private String gender;
    private String phone_number;
    private Long date_of_birth;
    private String personal_mail;
    private String company_mail;
    private String identify_id;
    private String issued_by;
    private Long issued_date;
    private String staying_address;
    private String residence_address;
    private String personal_tax_id;
    private Integer education_type;
    private String note;
    private String school_name;
    private String major;
    private Long graduation_date;
    private Integer married_status;
    private String bank_account_number;
    private String bank;
    private String bank_branch;
    private String agent_code;
    private String position_id;
    private String position_name; //để show ra
    private String working_branch;
    private String department_id;
    private String department_name; //để show ra
    private Long start_working_date;
    private Integer agent_status;
    private Integer agent_type;
    private Double total_date_off;
    private Integer role;
    @Builder.Default
    private Boolean is_deleted = false; //k cần
    private Long created_date; //k cần
    private Long last_updated_date; //k cần\

    @Builder.Default
    private Boolean is_used_happy_time = false; // khi đăng nhập bằng happy time sẽ đổi sang is_used_happy_time= true
    private Long stop_working_date;
    private String device_id;
    private ReferenceData create_by; //k cần
    private ReferenceData last_update_by; //k cần
    private String username;
    @Builder.Default
    private Boolean is_has_account = false;
    @Builder.Default
    private Boolean required_attendance = true;
    private LastLoginInfo last_login_info;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class LastLoginInfo {
        private String device_name;
        private Long time;
    }
}
