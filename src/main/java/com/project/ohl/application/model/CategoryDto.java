package com.project.ohl.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class CategoryDto {

    private UUID id;

    @NotBlank(message = "Name must not be blank")
    private String name;
    @Pattern(regexp = "^[a-z0-9_]+$")
    @Size(max = 255)
    private String mediaKey;

    @Builder.Default
    private Boolean isExpanded = false;

    @Builder.Default
    private Boolean isFavorite = false;

    private UUID parentId;

    @NotNull(message = "Section ID must not be null")
    private UUID sectionId;
}
