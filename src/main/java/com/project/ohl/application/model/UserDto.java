package com.project.ohl.application.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UserDto {

    private UUID id;
    private UUID classroomId;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 72, message = "Password is invalid")
    @ToString.Exclude
    private String password;

    @NotBlank(message = "Mobile must not be blank")
    @Size(min = 10, max = 10, message = "Mobile is invalid")
    private String mobile;

    @NotBlank(message = "Address must not be blank")
    private String address;

    private LocalDate birthday;

    @NotNull(message = "Institution ID must not be null")
    private UUID institutionId;

    @NotNull(message = "Role ID must not be null")
    private UUID roleId;
}
