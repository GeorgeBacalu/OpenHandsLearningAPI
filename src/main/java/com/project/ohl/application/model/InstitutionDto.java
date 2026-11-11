package com.project.ohl.application.model;

import com.project.ohl.domain.model.Institution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstitutionDto {

    @Positive(message = "Institution ID must be positive")
    private Long id;

    @NotBlank(message = "School must not be blank")
    private String school;

    @NotBlank(message = "Classroom must not be blank")
    private String classroom;
}
