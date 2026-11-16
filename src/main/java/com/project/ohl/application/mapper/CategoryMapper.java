package com.project.ohl.application.mapper;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.domain.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class)
public interface CategoryMapper {
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "isFavorite", ignore = true)
    CategoryDto convertToDto(Category model);
}
