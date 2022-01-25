package com.vadmack.mongodbtest.dto;

import com.vadmack.mongodbtest.entity.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    private String id;
    private String username;
    private Set<Role> authorities;
}
