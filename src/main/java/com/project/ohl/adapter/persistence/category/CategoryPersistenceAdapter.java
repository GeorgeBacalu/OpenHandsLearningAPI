package com.project.ohl.adapter.persistence.category;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.adapter.persistence.section.SectionEntity;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.domain.model.CatalogMetadata;
import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.Section;
import java.util.List;
import java.util.UUID;
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
    protected UUID idOf(Category model) {
        return model.getId();
    }

    @Override
    public Category toDomain(CategoryEntity entity) {
        return Category.builder().id(entity.getId())
              .name(entity.getName()).mediaKey(entity.getMediaKey())

              .isExpanded(false)
              .isFavorite(false)
              .parent(entity.getParent() == null ? null : Category.builder().id(entity.getParent().getId()).build())
              .section(entity.getSection() == null ? null : Section.builder().id(entity.getSection().getId()).build())
              .build();
    }

    @Override
    protected void apply(Category model, CategoryEntity entity) {
        entity.setName(model.getName());
        entity.setMediaKey(CatalogMetadata.mediaKey(model.getMediaKey(), entity.getMediaKey(), model.getName()));

        entity.setParent(reference(CategoryEntity.class, model.getParent() == null ? null : model.getParent().getId()));
        entity.setSection(reference(SectionEntity.class, model.getSection() == null ? null : model.getSection().getId()));
    }

    @Override
    public List<Category> findByParentIdNullAndSectionId(UUID sectionId) {
        return categories.findByParent_IdIsNullAndSection_Id(sectionId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findByParentIdNullAndSectionIdAndNameContaining(UUID sectionId, String name) {
        return categories.findByParent_IdIsNullAndSection_IdAndNameContainingIgnoreCase(sectionId, name).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findByParentIdAndSectionId(UUID parentId, UUID sectionId) {
        return categories.findByParent_IdAndSection_Id(parentId, sectionId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findByParentIdAndSectionIdAndNameContaining(UUID parentId, UUID sectionId, String name) {
        return categories.findByParent_IdAndSection_IdAndNameContainingIgnoreCase(parentId, sectionId, name).stream().map(this::toDomain).toList();
    }

}
