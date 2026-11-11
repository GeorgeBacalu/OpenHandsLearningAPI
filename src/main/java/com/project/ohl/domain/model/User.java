package com.project.ohl.domain.model;

import java.util.UUID;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private UUID id;
    private String name;
    private String email;
    @lombok.ToString.Exclude
    private String password;
    private String mobile;
    private String address;
    private LocalDate birthday;
    private Institution institution;
    private Role role;
}
