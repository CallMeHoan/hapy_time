package com.happy_time.happy_time.ddd.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TokenInfo {
    private String tenant_id;
    private String agent_id;
    private String usernname;
    private String role;
}
