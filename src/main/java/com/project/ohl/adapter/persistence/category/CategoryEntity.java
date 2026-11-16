package com.project.ohl.adapter.persistence.category;

import com.project.ohl.adapter.persistence.PersistentEntity;
import jakarta.persistence.CheckConstraint;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories", indexes = @Index(name = "idx_categories_parent", columnList = "parent_id"),
        check = @CheckConstraint(constraint = "trim(name) <> '' AND (parent_id IS NULL OR parent_id <> id)"))
public class CategoryEntity extends PersistentEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;
    @Column(name = "video_url", length = 2048)
    private String videoUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;
}
