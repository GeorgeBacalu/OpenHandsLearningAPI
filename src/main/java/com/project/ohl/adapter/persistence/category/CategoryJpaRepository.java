package com.project.ohl.adapter.persistence.category;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByParent_IdIsNull();
    List<CategoryEntity> findByParent_IdIsNullAndNameContainingIgnoreCase(String name);
    List<CategoryEntity> findByParent_Id(UUID parentId);
    List<CategoryEntity> findByParent_IdAndNameContainingIgnoreCase(UUID parentId, String name);
}
