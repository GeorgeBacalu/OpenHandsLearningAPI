package com.project.ohl.application.service;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.port.out.FavoriteRepositoryPort;
import com.project.ohl.application.port.out.UserRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Category;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.CATEGORY_NOT_FOUND;

@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private static final Collator ROMANIAN_COLLATOR = romanianCollator();

    private final CategoryRepositoryPort categoryRepository;

    private final CategoryMapper mapper;
    private final FavoriteRepositoryPort favorites;
    private final UserRepositoryPort users;

    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return alphabeticalCategories(categories);
    }

    @Override
    public CategoryDto getCategoryById(UUID id) {
        Category category = getCategoryEntityById(id);
        return mapper.convertToDto(category);
    }

    @Override
    public CategoryDto saveCategory(CategoryDto categoryDto) {
        validateParent(null, categoryDto.getParentId());
        Category category = mapper.convertToEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return mapper.convertToDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategoryById(CategoryDto categoryDto, UUID id) {
        Category categoryToUpdate = getCategoryEntityById(id);
        validateParent(id, categoryDto.getParentId());
        categoryToUpdate.setMediaKey(categoryDto.getMediaKey());
        categoryToUpdate.setName(categoryDto.getName());

        categoryToUpdate.setIsExpanded(categoryDto.getIsExpanded());
        categoryToUpdate.setIsFavorite(categoryDto.getIsFavorite());
        categoryToUpdate.setParent(categoryDto.getParentId() == null ? null : getCategoryEntityById(categoryDto.getParentId()));
        Category updatedCategory = categoryRepository.update(categoryToUpdate);
        return mapper.convertToDto(updatedCategory);
    }

    @Override
    public void deleteCategoryById(UUID id) {
        Category categoryToDelete = getCategoryEntityById(id);
        categoryRepository.delete(categoryToDelete);
    }

    @Override
    public List<CategoryDto> getCategoriesByParentIdAndName(UUID parentId, String name) {
        List<Category> categories;
        if(parentId == null) {
            categories = name == null || name.isBlank()
                  ? categoryRepository.findByParentIdNull()
                  : categoryRepository.findByParentIdNullAndNameContaining(name);
        } else {
            categories = name == null || name.isBlank()
                  ? categoryRepository.findByParentId(parentId)
                  : categoryRepository.findByParentIdAndNameContaining(parentId, name);
        }
        return alphabeticalCategories(categories);
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
    public List<CategoryDto> getFavoriteCategories(UUID userId) { return getFavoritesByName(userId, ""); }

    @Override
    public List<CategoryDto> getFavoritesByName(UUID userId, String name) {
        requireUser(userId);
        var result = alphabeticalCategories(favorites.findByUserAndName(userId, name == null ? "" : name));
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

    private List<CategoryDto> alphabeticalCategories(List<Category> categories) {
        var collator = (Collator) ROMANIAN_COLLATOR.clone();
        Comparator<Category> byName = Comparator.comparing(category -> category.getName().strip().toLowerCase(Locale.ROOT), collator);
        return categories.stream().sorted(byName.thenComparing(Category::getId))
                .map(mapper::convertToDto).toList();
    }

    private static Collator romanianCollator() {
        try {
            var base = (RuleBasedCollator) Collator.getInstance(Locale.forLanguageTag("ro-RO"));
            var collator = new RuleBasedCollator(base.getRules()
                    + "& a < ă , Ă < â , Â & i < î , Î & s < ș , Ș = ş , Ş & t < ț , Ț = ţ , Ţ");
            collator.setStrength(Collator.SECONDARY);
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            return collator;
        } catch (ParseException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private void requireUser(UUID id) {
        if (id == null || users.findById(id).isEmpty()) throw new ResourceNotFoundException("User " + id + " was not found");
    }

    private void validateParent(UUID categoryId, UUID parentId) {
        var visited = new HashSet<UUID>();
        while (parentId != null) {
            if (parentId.equals(categoryId) || !visited.add(parentId)) {
                throw new InvalidRequestException("Category hierarchy must not contain cycles.");
            }
            var current = getCategoryEntityById(parentId);
            var parent = current.getParent();
            parentId = parent == null ? null : parent.getId();
        }
    }

    private Category getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(CATEGORY_NOT_FOUND, id)));
    }
}
