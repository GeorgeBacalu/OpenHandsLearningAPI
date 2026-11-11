package com.project.ohl.application.port.in;

import java.util.UUID;
import com.project.ohl.application.model.CategoryDto;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> getAllCategories();

    CategoryDto getCategoryById(UUID id);

    CategoryDto saveCategory(CategoryDto categoryDto);

    CategoryDto updateCategoryById(CategoryDto categoryDto, UUID id);

    void deleteCategoryById(UUID id);

    List<CategoryDto> getCategoriesByParentIdAndSectionIdAndName(UUID parentId, UUID sectionId, String name);

    List<CategoryDto> getFavoriteCategories();

    List<CategoryDto> getFavoritesByName(String name);

    CategoryDto saveFavorite(UUID categoryId);

    CategoryDto deleteFavorite(UUID categoryId);
}
