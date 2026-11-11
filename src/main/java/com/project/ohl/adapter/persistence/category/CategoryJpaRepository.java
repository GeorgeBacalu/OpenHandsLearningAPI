package com.project.ohl.adapter.persistence.category;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParent_IdIsNullAndSection_IdOrderByIdAsc(Long sectionId);
    List<CategoryEntity> findByParent_IdIsNullAndSection_IdAndNameContainingIgnoreCaseOrderByIdAsc(Long sectionId, String name);
    List<CategoryEntity> findByParent_IdAndSection_IdOrderByIdAsc(Long parentId, Long sectionId);
    List<CategoryEntity> findByParent_IdAndSection_IdAndNameContainingIgnoreCaseOrderByIdAsc(Long parentId, Long sectionId, String name);
    List<CategoryEntity> findByIsFavoriteTrueOrderByIdAsc();
    List<CategoryEntity> findByIsFavoriteTrueAndNameContainingIgnoreCaseOrderByIdAsc(String name);
}
