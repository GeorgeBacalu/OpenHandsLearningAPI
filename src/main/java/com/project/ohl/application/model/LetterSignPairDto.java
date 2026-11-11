package com.project.ohl.application.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
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

    @NotEmpty(message = "Letter must not be empty")
    private String letter;

    @Positive(message = "Image ID must be positive")
    private Integer imageId;
}
