package com.project.llt.category;

import com.project.llt.section.Section;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {

    private Long id;
    private String name;
    private Integer imageId;
    private Boolean isExpanded;
    private Boolean isFavorite;
    private Category parent;
    private Section section;
}
