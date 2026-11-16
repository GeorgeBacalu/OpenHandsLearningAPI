package com.project.ohl.adapter.transaction;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.in.CategoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionalCategoryService implements CategoryService {
    private final CategoryService delegate;

    @Override
    public List<CategoryDto> getAllCategories() {
        return delegate.getAllCategories();
    }

    @Override
    public CategoryDto getCategoryById(UUID id) {
        return delegate.getCategoryById(id);
    }

    @Override
    public CategoryDto getCategoryById(UUID id, UUID userId) {
        return delegate.getCategoryById(id, userId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryById(CategoryDto categoryDto, UUID id) {
        return delegate.updateCategoryById(categoryDto, id);
    }

    @Override
    @Transactional
    public void deleteCategoryById(UUID id) {
        delegate.deleteCategoryById(id);
    }

    @Override
    public List<CategoryDto> getCategoriesByParentIdAndName(UUID parentId, String name) {
        return delegate.getCategoriesByParentIdAndName(parentId, name);
    }

    @Override
    public List<CategoryDto> getFavoriteCategories(UUID userId) {
        return delegate.getFavoriteCategories(userId);
    }

    @Override
    public List<CategoryDto> getFavoritesByName(UUID userId, String name) {
        return delegate.getFavoritesByName(userId, name);
    }

    @Override
    @Transactional
    public CategoryDto saveFavorite(UUID userId, UUID categoryId) {
        return delegate.saveFavorite(userId, categoryId);
    }

    @Override
    @Transactional
    public CategoryDto deleteFavorite(UUID userId, UUID categoryId) {
        return delegate.deleteFavorite(userId, categoryId);
    }
}
