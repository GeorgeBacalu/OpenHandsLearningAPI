package com.project.ohl.application.model;

import java.util.UUID;
import com.project.ohl.domain.model.Section;

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
public class SectionDto {

    private UUID id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @Positive(message = "Icon ID must be positive")
    private Integer iconId;

    @Positive(message = "Image ID must be positive")
    private Integer imageId;
}
