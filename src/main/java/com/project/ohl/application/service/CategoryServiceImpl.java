package com.project.ohl.application.service;

import java.util.UUID;
import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.domain.model.Category;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.application.port.in.SectionService;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.CATEGORY_NOT_FOUND;

@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepositoryPort categoryRepository;
    private final SectionService sectionService;

    private final CategoryMapper mapper;

    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return mapper.convertToDtoList(categories);
    }

    @Override
    public CategoryDto getCategoryById(UUID id) {
        Category category = getCategoryEntityById(id);
        return mapper.convertToDto(category);
    }

    @Override
    public CategoryDto saveCategory(CategoryDto categoryDto) {
        Category category = mapper.convertToEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return mapper.convertToDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategoryById(CategoryDto categoryDto, UUID id) {
        Category categoryToUpdate = getCategoryEntityById(id);
        validateParent(id, categoryDto.getParentId());
        categoryToUpdate.setName(categoryDto.getName());
        categoryToUpdate.setImageId(categoryDto.getImageId());
        categoryToUpdate.setIsExpanded(categoryDto.getIsExpanded());
        categoryToUpdate.setIsFavorite(categoryDto.getIsFavorite());
        categoryToUpdate.setParent(categoryDto.getParentId() == null ? null : getCategoryEntityById(categoryDto.getParentId()));
        categoryToUpdate.setSection(sectionService.getSectionEntityById(categoryDto.getSectionId()));
        Category updatedCategory = categoryRepository.update(categoryToUpdate);
        return mapper.convertToDto(updatedCategory);
    }

    @Override
    public void deleteCategoryById(UUID id) {
        Category categoryToDelete = getCategoryEntityById(id);
        categoryRepository.delete(categoryToDelete);
    }

    @Override
    public List<CategoryDto> getCategoriesByParentIdAndSectionIdAndName(UUID parentId, UUID sectionId, String name) {
        List<Category> categories;
        if(parentId == null) {
            categories = name == null || name.isBlank()
                  ? categoryRepository.findByParentIdNullAndSectionId(sectionId)
                  : categoryRepository.findByParentIdNullAndSectionIdAndNameContaining(sectionId, name);
        } else {
            categories = name == null || name.isBlank()
                  ? categoryRepository.findByParentIdAndSectionId(parentId, sectionId)
                  : categoryRepository.findByParentIdAndSectionIdAndNameContaining(parentId, sectionId, name);
        }
        return mapper.convertToDtoList(categories);
    }

    @Override
    public List<CategoryDto> getFavoriteCategories() {
        List<Category> favorites = categoryRepository.findFavorites();
        return mapper.convertToDtoList(favorites);
    }

    @Override
    public List<CategoryDto> getFavoritesByName(String name) {
        List<Category> filteredFavorites = name == null || name.isBlank()
              ? categoryRepository.findFavorites()
              : categoryRepository.findFavoritesByNameContaining(name);
        return mapper.convertToDtoList(filteredFavorites);
    }

    @Override
    public CategoryDto saveFavorite(UUID categoryId) {
        Category favoriteToSave = getCategoryEntityById(categoryId);
        favoriteToSave.setIsFavorite(true);
        return mapper.convertToDto(categoryRepository.update(favoriteToSave));
    }

    @Override
    public CategoryDto deleteFavorite(UUID categoryId) {
        Category favoriteToDelete = getCategoryEntityById(categoryId);
        favoriteToDelete.setIsFavorite(false);
        return mapper.convertToDto(categoryRepository.update(favoriteToDelete));
    }

    private void validateParent(UUID categoryId, UUID parentId) {
        var visited = new java.util.HashSet<UUID>();
        while (parentId != null) {
            if (parentId.equals(categoryId) || !visited.add(parentId)) {
                throw new com.project.ohl.domain.exception.InvalidRequestException("Category hierarchy must not contain cycles.");
            }
            var parent = getCategoryEntityById(parentId).getParent();
            parentId = parent == null ? null : parent.getId();
        }
    }

    private Category getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(CATEGORY_NOT_FOUND, id)));
    }
}
