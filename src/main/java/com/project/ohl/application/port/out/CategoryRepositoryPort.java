package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Category save(Category category);

    Category update(Category category);

    void delete(Category category);

    List<Category> findByParentIdNullAndSectionId(Long sectionId);

    List<Category> findByParentIdNullAndSectionIdAndNameContaining(Long sectionId, String name);

    List<Category> findByParentIdAndSectionId(Long parentId, Long sectionId);

    List<Category> findByParentIdAndSectionIdAndNameContaining(Long parentId, Long sectionId, String name);

    List<Category> findFavorites();

    List<Category> findFavoritesByNameContaining(String name);
}
