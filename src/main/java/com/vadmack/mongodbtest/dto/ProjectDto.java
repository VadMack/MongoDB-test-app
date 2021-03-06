package com.vadmack.mongodbtest.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProjectDto {
    private Long id;
    private String name;
}
