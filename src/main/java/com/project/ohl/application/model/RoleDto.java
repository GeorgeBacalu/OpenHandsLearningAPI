package com.project.ohl.application.model;

import com.project.ohl.domain.model.Authority;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {

    private UUID id;

    @NotNull(message = "Authority must not be null")
    private Authority authority;
}
