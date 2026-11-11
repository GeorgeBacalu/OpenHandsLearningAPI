package com.project.llt.mapper;

import com.project.llt.category.Category;
import com.project.llt.category.CategoryDao;
import com.project.llt.category.CategoryDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

    public static CategoryDto convertToDto(ModelMapper modelMapper, Category category) {
        CategoryDto categoryDto = modelMapper.map(category, CategoryDto.class);
        categoryDto.setParentId(category.getId());
        return categoryDto;
    }

    public static Category convertToEntity(ModelMapper modelMapper, CategoryDto categoryDto, CategoryDao categoryDao) {
        Category category = modelMapper.map(categoryDto, Category.class);
        category.setParent(categoryDao.findById(categoryDto.getParentId()).orElse(null));
        return category;
    }

    public static List<CategoryDto> convertToDtoList(ModelMapper modelMapper, List<Category> categories) {
        return !categories.isEmpty() ? categories.stream().map(category -> convertToDto(modelMapper, category)).toList() : new ArrayList<>();
    }
}
