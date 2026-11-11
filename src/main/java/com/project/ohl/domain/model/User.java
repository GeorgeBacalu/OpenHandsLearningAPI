package com.project.ohl.domain.model;

import java.time.LocalDate;
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
    private UUID classroomId;
    private String name;
    private String email;
    @ToString.Exclude
    private String password;
    private String mobile;
    private String address;
    private LocalDate birthday;
    private School school;
    private Role role;
}
