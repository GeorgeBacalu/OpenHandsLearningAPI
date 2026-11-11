package com.project.ohl.application.port.in;

import com.project.ohl.application.model.CategoryDto;
import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryDto> getAllCategories();

    CategoryDto getCategoryById(UUID id);
    CategoryDto getCategoryById(UUID id, UUID userId);

    CategoryDto saveCategory(CategoryDto categoryDto);

    CategoryDto updateCategoryById(CategoryDto categoryDto, UUID id);

    void deleteCategoryById(UUID id);

    List<CategoryDto> getCategoriesByParentIdAndSectionIdAndName(UUID parentId, UUID sectionId, String name);

    List<CategoryDto> getFavoriteCategories(UUID userId);

    List<CategoryDto> getFavoritesByName(UUID userId, String name);

    CategoryDto saveFavorite(UUID userId, UUID categoryId);

    CategoryDto deleteFavorite(UUID userId, UUID categoryId);
}
