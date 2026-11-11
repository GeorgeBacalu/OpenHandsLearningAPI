package com.project.llt.category;

import jakarta.validation.constraints.NotBlank;
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
public class CategoryDto {

    @Positive(message = "Category ID must be positive")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @Positive(message = "Image ID must be positive")
    private Integer imageId;

    @Builder.Default
    @NotNull(message = "IsExpanded must not be null")
    private Boolean isExpanded = false;

    @Builder.Default
    @NotNull(message = "IsFavorite must not be null")
    private Boolean isFavorite = false;

    private Long parentId;

    @Positive(message = "Section ID must be positive")
    @NotNull(message = "Section ID must not be null")
    private Long sectionId;
}
