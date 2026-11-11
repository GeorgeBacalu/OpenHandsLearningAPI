package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Category;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.domain.model.Section;
import java.util.List;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryDto convertToDto(Category model) {
        return CategoryDto.builder().id(model.getId())
              .name(model.getName())
              .imageId(model.getImageId())
              .isExpanded(model.getIsExpanded())
              .isFavorite(model.getIsFavorite())
              .parentId(model.getParent() == null ? null : model.getParent().getId())
              .sectionId(model.getSection() == null ? null : model.getSection().getId())
              .build();
    }

    public static Category convertToEntity(CategoryDto dto) {
        return Category.builder().id(dto.getId())
              .name(dto.getName())
              .imageId(dto.getImageId())
              .isExpanded(dto.getIsExpanded())
              .isFavorite(dto.getIsFavorite())
              .parent(dto.getParentId() == null ? null : Category.builder().id(dto.getParentId()).build())
              .section(dto.getSectionId() == null ? null : Section.builder().id(dto.getSectionId()).build())
              .build();
    }

    public static List<CategoryDto> convertToDtoList(List<Category> categories) {
        return categories.stream().map(CategoryMapper::convertToDto).toList();
    }
}
