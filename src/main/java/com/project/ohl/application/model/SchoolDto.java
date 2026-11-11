package com.project.ohl.application.model;

import jakarta.validation.constraints.NotBlank;
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
public class SchoolDto {

    private UUID id;

    @NotBlank(message = "School must not be blank")
    private String name;
    @NotNull
    private UUID cityId;

}
