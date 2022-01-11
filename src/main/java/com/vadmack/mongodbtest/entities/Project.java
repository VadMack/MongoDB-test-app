package com.vadmack.mongodbtest.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Document("projects")
public class Project {
    @Transient
    public static final String SEQUENCE_NAME = "projects_sequence";

    @Id
    private Long id;

    private String name;
}
