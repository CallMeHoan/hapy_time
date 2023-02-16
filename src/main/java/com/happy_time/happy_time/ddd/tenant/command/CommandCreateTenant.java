package com.happy_time.happy_time.ddd.tenant.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommandCreateTenant {
    private String company_name;
    private String company_shorthand;
    private Long scale;
    private String company_mail;
    private String website;
    private String tax_number;
    private String fanpage;
    private String hotline;
    private String avatar;
    private String head_position_id;
}
