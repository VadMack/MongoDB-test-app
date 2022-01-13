package com.vadmack.mongodbtest.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProjectNoIdDto {
    @NotBlank(message = "The property 'name' is not defined")
    private String name;
}
