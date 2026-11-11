package com.project.ohl.adapter.persistence.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
    Optional<CategoryEntity> findByCode(String code);
    List<CategoryEntity> findByParent_IdIsNullAndSection_IdOrderByDisplayOrderAscIdAsc(UUID sectionId);
    List<CategoryEntity> findByParent_IdIsNullAndSection_IdAndNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(UUID sectionId, String name);
    List<CategoryEntity> findByParent_IdAndSection_IdOrderByDisplayOrderAscIdAsc(UUID parentId, UUID sectionId);
    List<CategoryEntity> findByParent_IdAndSection_IdAndNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(UUID parentId, UUID sectionId, String name);
}
