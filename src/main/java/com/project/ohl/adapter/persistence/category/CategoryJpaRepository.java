package com.project.ohl.adapter.persistence.category;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentIdIsNullAndSectionIdOrderByIdAsc(Long sectionId);
    List<CategoryEntity> findByParentIdIsNullAndSectionIdAndNameContainingIgnoreCaseOrderByIdAsc(Long sectionId, String name);
    List<CategoryEntity> findByParentIdAndSectionIdOrderByIdAsc(Long parentId, Long sectionId);
    List<CategoryEntity> findByParentIdAndSectionIdAndNameContainingIgnoreCaseOrderByIdAsc(Long parentId, Long sectionId, String name);
    List<CategoryEntity> findByIsFavoriteTrueOrderByIdAsc();
    List<CategoryEntity> findByIsFavoriteTrueAndNameContainingIgnoreCaseOrderByIdAsc(String name);
}
