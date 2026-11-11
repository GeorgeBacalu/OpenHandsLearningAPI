package com.project.llt.user;

import com.project.llt.institution.Institution;
import com.project.llt.role.Role;
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

    private Long id;
    private String name;
    private String email;
    private String password;
    private String mobile;
    private String address;
    private LocalDate birthday;
    private Institution institution;
    private Role role;
}
