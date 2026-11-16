package com.project.ohl.application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.project.ohl.domain.model.CatalogMetadata;
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
    @Pattern(regexp = CatalogMetadata.IMAGE_URL_PATTERN)
    @Size(max = 2048)
    private String imageUrl;
    @Pattern(regexp = CatalogMetadata.VIDEO_URL_PATTERN)
    @Size(max = 2048)
    private String videoUrl;

    @Builder.Default
    private Boolean isExpanded = false;

    @Builder.Default
    private Boolean isFavorite = false;

    private UUID parentId;

}
