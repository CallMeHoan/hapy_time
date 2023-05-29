package com.happy_time.happy_time.ddd.news.news;

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
@Document(collection = "new")
public class New implements Serializable {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId _id;

    private String tenant_id;
    @Builder.Default
    private Boolean is_deleted = false;
    private ReferenceData create_by;
    private ReferenceData last_update_by;
    private Long created_date;
    private Long last_updated_date;
    private String title;
    private String title_unsigned;
    private String category_id;
    private String status;
    private String banner;
    private String content;
    private String category_name;
    @Builder.Default
    private Integer total_views = 0;
    @Builder.Default
    private Integer total_likes = 0;
    @Builder.Default
    private Integer total_replies = 0;
    private Long post_date;
}
