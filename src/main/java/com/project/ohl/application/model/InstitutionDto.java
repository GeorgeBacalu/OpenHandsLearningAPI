package com.project.ohl.application.model;

import java.util.UUID;
import com.project.ohl.domain.model.Institution;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstitutionDto {

    private UUID id;

    @NotBlank(message = "School must not be blank")
    private String school;

    @NotBlank(message = "Classroom must not be blank")
    private String classroom;
}
