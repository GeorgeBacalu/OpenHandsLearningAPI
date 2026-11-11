package com.project.ohl.application.model;

import java.util.UUID;
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
public class LetterSignPairDto {

    private UUID id;

    @NotBlank(message = "Letter must not be blank")
    private String letter;

    @Positive(message = "Image ID must be positive")
    private Integer imageId;
}
