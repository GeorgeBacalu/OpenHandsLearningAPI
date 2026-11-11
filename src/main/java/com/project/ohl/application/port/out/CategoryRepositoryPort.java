package com.project.ohl.application.port.out;

import java.util.UUID;
import com.project.ohl.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {

    List<Category> findAll();

    Optional<Category> findById(UUID id);

    Category save(Category category);

    Category update(Category category);

    void delete(Category category);

    List<Category> findByParentIdNullAndSectionId(UUID sectionId);

    List<Category> findByParentIdNullAndSectionIdAndNameContaining(UUID sectionId, String name);

    List<Category> findByParentIdAndSectionId(UUID parentId, UUID sectionId);

    List<Category> findByParentIdAndSectionIdAndNameContaining(UUID parentId, UUID sectionId, String name);

    List<Category> findFavorites();

    List<Category> findFavoritesByNameContaining(String name);
}
