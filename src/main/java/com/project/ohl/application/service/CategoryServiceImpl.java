package com.project.ohl.application.service;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.in.SectionService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.FavoriteRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Category;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.CATEGORY_NOT_FOUND;

@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepositoryPort categoryRepository;
    private final SectionService sectionService;

    private final CategoryMapper mapper;
    private final FavoriteRepositoryPort favorites;
    private final UserRepositoryPort users;

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
        validateParent(null, categoryDto.getParentId(), categoryDto.getSectionId());
        Category category = mapper.convertToEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return mapper.convertToDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategoryById(CategoryDto categoryDto, UUID id) {
        Category categoryToUpdate = getCategoryEntityById(id);
        validateParent(id, categoryDto.getParentId(), categoryDto.getSectionId());
        categoryToUpdate.setCode(categoryDto.getCode());
        categoryToUpdate.setMediaKey(categoryDto.getMediaKey());
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
    public CategoryDto getCategoryById(UUID id, UUID userId) {
        CategoryDto result = getCategoryById(id);
        if (userId != null) {
            requireUser(userId);
            result.setIsFavorite(favorites.exists(userId, id));
        }
        return result;
    }

    @Override
    public List<CategoryDto> getCategoriesByFilters(UUID parentId, UUID sectionId, String parentCode, String sectionCode, String name) {
        if (parentCode != null) parentId = categoryRepository.findByCode(parentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Category " + parentCode + " was not found")).getId();
        if (sectionId == null) sectionId = sectionService.getSectionEntityByCode(sectionCode == null ? "VIDEO" : sectionCode).getId();
        return getCategoriesByParentIdAndSectionIdAndName(parentId, sectionId, name);
    }

    @Override
    public List<CategoryDto> getFavoriteCategories(UUID userId) { return getFavoritesByName(userId, ""); }

    @Override
    public List<CategoryDto> getFavoritesByName(UUID userId, String name) {
        requireUser(userId);
        var result = mapper.convertToDtoList(favorites.findByUserAndName(userId, name == null ? "" : name));
        result.forEach(category -> category.setIsFavorite(true));
        return result;
    }

    @Override
    public CategoryDto saveFavorite(UUID userId, UUID categoryId) {
        requireUser(userId);
        CategoryDto result = getCategoryById(categoryId);
        favorites.save(userId, categoryId);
        result.setIsFavorite(true);
        return result;
    }

    @Override
    public CategoryDto deleteFavorite(UUID userId, UUID categoryId) {
        requireUser(userId);
        CategoryDto result = getCategoryById(categoryId);
        favorites.delete(userId, categoryId);
        result.setIsFavorite(false);
        return result;
    }

    private void requireUser(UUID id) {
        if (id == null || users.findById(id).isEmpty()) throw new ResourceNotFoundException("User " + id + " was not found");
    }

    private void validateParent(UUID categoryId, UUID parentId, UUID sectionId) {
        var visited = new HashSet<UUID>();
        while (parentId != null) {
            if (parentId.equals(categoryId) || !visited.add(parentId)) {
                throw new InvalidRequestException("Category hierarchy must not contain cycles.");
            }
            var current = getCategoryEntityById(parentId);
            if (current.getSection() != null && !Objects.equals(current.getSection().getId(), sectionId))
                throw new InvalidRequestException("Parent and child must belong to the same section.");
            var parent = current.getParent();
            parentId = parent == null ? null : parent.getId();
        }
    }

    private Category getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(CATEGORY_NOT_FOUND, id)));
    }
}
