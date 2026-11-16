package com.project.ohl.application.model;

import com.project.ohl.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UserDto {

    private UUID id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email
    private String email;

    @NotBlank(message = "Password must not be blank")
    @ToString.Exclude
    private String password;

    @NotBlank(message = "Phone must not be blank")
    @Size(min = 10, max = 10, message = "Phone is invalid")
    private String phone;

    @NotNull(message = "School ID must not be null")
    private UUID schoolId;

    @NotNull(message = "Role must not be null")
    private Role role;
}
