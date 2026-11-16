package com.project.ohl.application.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public record Registration(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank String password,
        @NotBlank @Pattern(regexp = "0[0-9]{9}") String phone,
        @NotNull UUID schoolId) {
    @Override public @NonNull String toString() { return "Registration[email=" + email + "]"; }
}
