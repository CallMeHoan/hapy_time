package com.happy_time.happy_time.agent.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;
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
    Object _id;
    private String name;
}
