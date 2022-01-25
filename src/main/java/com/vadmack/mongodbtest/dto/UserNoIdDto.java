package com.vadmack.mongodbtest.dto;

import com.vadmack.mongodbtest.entity.Role;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class UserNoIdDto {
    @NotBlank(message = "The property 'name' is not defined")
    private String username;
    @NotBlank(message = "The property 'password' is not defined")
    private String password;
    @NotEmpty(message = "The property 'authorities' is not defined")
    private Set<Role> authorities;
}
