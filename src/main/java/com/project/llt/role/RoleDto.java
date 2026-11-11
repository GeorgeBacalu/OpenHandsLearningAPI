package com.project.llt.role;

import com.project.llt.role.enums.Authority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {

    @Positive(message = "Role ID must be positive")
    private Long id;

    @NotNull(message = "Authority must not be null")
    private Authority authority;
}
