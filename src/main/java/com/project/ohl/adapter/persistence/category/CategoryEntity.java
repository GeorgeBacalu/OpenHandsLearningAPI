package com.project.ohl.adapter.persistence.category;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.section.SectionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories", indexes = {
      @Index(name = "idx_categories_section_parent", columnList = "section_id,parent_id,id"),
      @Index(name = "idx_categories_favorites", columnList = "is_favorite,id")
})
public class CategoryEntity extends PersistentEntity {
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = Integer.MAX_VALUE;
    @Column(name = "name")
    private String name;
    @Column(name = "image_id")
    private Integer imageId;
    @Column(name = "is_expanded", nullable = false)
    private Boolean isExpanded;
    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private SectionEntity section;
}
