package com.project.llt.category;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> getAllCategories();

    CategoryDto getCategoryById(Long id);

    CategoryDto saveCategory(CategoryDto categoryDto);

    CategoryDto updateCategoryById(CategoryDto categoryDto, Long id);

    void deleteCategoryById(Long id);

    List<CategoryDto> getCategoriesByParentIdAndSectionIdAndName(Long parentId, Long sectionId, String name);

    List<CategoryDto> getFavoriteCategories();

    List<CategoryDto> getFavoritesByName(String name);

    CategoryDto saveFavorite(Long categoryId);

    CategoryDto deleteFavorite(Long categoryId);
}
