package com.project.llt.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    @Positive(message = "User ID must be positive")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 25, message = "Password is invalid")
    private String password;

    @NotBlank(message = "Mobile must not be blank")
    @Size(min = 10, max = 10, message = "Mobile is invalid")
    private String mobile;

    @NotBlank(message = "Address must not be blank")
    private String address;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotNull(message = "Institution ID must not be null")
    @Positive(message = "Institution ID must be positive")
    private Long institutionId;

    @NotNull(message = "Role ID must not be null")
    @Positive(message = "Role ID must be positive")
    private Long roleId;
}
