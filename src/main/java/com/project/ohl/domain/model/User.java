package com.project.ohl.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private UUID id;
    private String name;
    private String email;
    @ToString.Exclude
    private String passwordHash;
    private String phone;
    private School school;
    private Role role;
}
