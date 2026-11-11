package com.project.ohl.adapter.persistence.category;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByParent_IdIsNullAndSection_IdOrderByDisplayOrderAscIdAsc(UUID sectionId);
    List<CategoryEntity> findByParent_IdIsNullAndSection_IdAndNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(UUID sectionId, String name);
    List<CategoryEntity> findByParent_IdAndSection_IdOrderByDisplayOrderAscIdAsc(UUID parentId, UUID sectionId);
    List<CategoryEntity> findByParent_IdAndSection_IdAndNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(UUID parentId, UUID sectionId, String name);
    List<CategoryEntity> findByIsFavoriteTrueOrderByDisplayOrderAscIdAsc();
    List<CategoryEntity> findByIsFavoriteTrueAndNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(String name);
}
