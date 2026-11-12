package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {

    List<Category> findAll();

    Optional<Category> findById(UUID id);

    Category save(Category category);

    Category update(Category category);

    void delete(Category category);

    List<Category> findByParentIdNull();

    List<Category> findByParentIdNullAndNameContaining(String name);

    List<Category> findByParentId(UUID parentId);

    List<Category> findByParentIdAndNameContaining(UUID parentId, String name);

}
