package com.project.ohl.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;
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
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$")
    @Size(max = 120)
    private String code;
    private String mediaKey;
    private String iconKey;

    @Positive(message = "Icon ID must be positive")
    private Integer iconId;

    @Positive(message = "Image ID must be positive")
    private Integer imageId;
}
