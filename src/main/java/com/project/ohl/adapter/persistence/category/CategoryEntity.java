package com.project.ohl.adapter.persistence.category;

import com.project.ohl.adapter.persistence.PersistentEntity;
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
@Table(name = "categories", indexes = @Index(name = "idx_categories_parent", columnList = "parent_id"))
@Check(constraints = "trim(name) <> '' AND (parent_id IS NULL OR parent_id <> id)")
public class CategoryEntity extends PersistentEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "media_key", nullable = false)
    private String mediaKey;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;
}
