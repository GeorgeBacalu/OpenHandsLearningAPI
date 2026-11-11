package com.project.ohl.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {

    private UUID id;
    private String name;
    private String code;
    private String mediaKey;
    private Boolean isExpanded;
    private Boolean isFavorite;
    private Category parent;
    private Section section;
}
