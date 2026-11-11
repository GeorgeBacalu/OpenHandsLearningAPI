package com.project.ohl.adapter.persistence.category;

import com.project.ohl.adapter.persistence.PersistentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class CategoryEntity extends PersistentEntity {
    @Column(name = "name")
    private String name;
    @Column(name = "image_id")
    private Integer imageId;
    @Column(name = "is_expanded")
    private Boolean isExpanded;
    @Column(name = "is_favorite")
    private Boolean isFavorite;
    @Column(name = "parent_id")
    private Long parentId;
    @Column(name = "section_id")
    private Long sectionId;
}
