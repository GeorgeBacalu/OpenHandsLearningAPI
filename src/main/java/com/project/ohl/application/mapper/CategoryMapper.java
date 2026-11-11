package com.project.ohl.application.mapper;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.domain.model.Category;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class, uses = ReferenceMapper.class)
public interface CategoryMapper {
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "sectionId", source = "section.id")
    CategoryDto convertToDto(Category model);

    @Mapping(target = "parent", source = "parentId")
    @Mapping(target = "section", source = "sectionId")
    Category convertToEntity(CategoryDto dto);

    List<CategoryDto> convertToDtoList(List<Category> categories);
}
