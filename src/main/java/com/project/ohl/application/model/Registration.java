package com.project.ohl.application.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record Registration(@NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 15, max = 72) String password,
        @NotBlank @Pattern(regexp = "0[0-9]{9}") String phone, @NotNull UUID schoolId) {
    @Override public String toString() { return "Registration[email=" + email + "]"; }
}
