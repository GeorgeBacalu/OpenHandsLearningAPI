package com.project.ohl.adapter.persistence.category;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.domain.model.Category;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.domain.model.Section;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CategoryPersistenceAdapter extends JpaPersistenceAdapter<Category, CategoryEntity> implements CategoryRepositoryPort {
    private final CategoryJpaRepository categories;

    public CategoryPersistenceAdapter(CategoryJpaRepository repository) {
        super(repository);
        this.categories = repository;
    }

    @Override
    protected CategoryEntity newEntity() {
        return new CategoryEntity();
    }

    @Override
    protected Long idOf(Category model) {
        return model.getId();
    }

    @Override
    protected Category toDomain(CategoryEntity entity) {
        return Category.builder().id(entity.getId())
              .name(entity.getName())
              .imageId(entity.getImageId())
              .isExpanded(Boolean.TRUE.equals(entity.getIsExpanded()))
              .isFavorite(Boolean.TRUE.equals(entity.getIsFavorite()))
              .parent(entity.getParentId() == null ? null : Category.builder().id(entity.getParentId()).build())
              .section(entity.getSectionId() == null ? null : Section.builder().id(entity.getSectionId()).build())
              .build();
    }

    @Override
    protected void apply(Category model, CategoryEntity entity) {
        entity.setName(model.getName());
        entity.setImageId(model.getImageId());
        entity.setIsExpanded(Boolean.TRUE.equals(model.getIsExpanded()));
        entity.setIsFavorite(Boolean.TRUE.equals(model.getIsFavorite()));
        entity.setParentId(model.getParent() == null ? null : model.getParent().getId());
        entity.setSectionId(model.getSection() == null ? null : model.getSection().getId());
    }

    @Override
    public List<Category> findByParentIdNullAndSectionId(Long sectionId) {
        return categories.findByParentIdIsNullAndSectionIdOrderByIdAsc(sectionId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findByParentIdNullAndSectionIdAndNameContaining(Long sectionId, String name) {
        return categories.findByParentIdIsNullAndSectionIdAndNameContainingIgnoreCaseOrderByIdAsc(sectionId, name).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findByParentIdAndSectionId(Long parentId, Long sectionId) {
        return categories.findByParentIdAndSectionIdOrderByIdAsc(parentId, sectionId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findByParentIdAndSectionIdAndNameContaining(Long parentId, Long sectionId, String name) {
        return categories.findByParentIdAndSectionIdAndNameContainingIgnoreCaseOrderByIdAsc(parentId, sectionId, name).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findFavorites() {
        return categories.findByIsFavoriteTrueOrderByIdAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findFavoritesByNameContaining(String name) {
        return categories.findByIsFavoriteTrueAndNameContainingIgnoreCaseOrderByIdAsc(name).stream().map(this::toDomain).toList();
    }
}
