package com.project.ohl.adapter.persistence.category;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.section.SectionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories", indexes = {
      @Index(name = "idx_categories_section_parent_order", columnList = "section_id,parent_id,display_order,id"),
      @Index(name = "idx_categories_parent", columnList = "parent_id")
})
@Check(constraints = "trim(name) <> '' AND display_order >= 0 AND (parent_id IS NULL OR parent_id <> id)")
public class CategoryEntity extends PersistentEntity {
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = Integer.MAX_VALUE;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "code", nullable = false, unique = true, length = 120)
    private String code;
    @Column(name = "media_key", nullable = false)
    private String mediaKey;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;
}
