package com.happy_time.happy_time.ddd.auth.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "accounts")
public class Account implements UserDetails {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;
    private String tenant_id;
    private String agent_id;
    private String phone_number; // dùng để đăng nhập
    private String password;
    private String name; //name của agent
    private String status;
    private String role;
    private Boolean is_deleted;
    private Long created_date;
    private Long last_updated_date;
    @Builder.Default
    private Boolean changed_password = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
