package com.project.ohl.mapper;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.domain.model.Category;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.project.ohl.TestIds.id;
import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {
    private final CategoryMapper categories = Mappers.getMapper(CategoryMapper.class);
    private final UserMapper users = Mappers.getMapper(UserMapper.class);

    @Test
    void categoryMappingPreservesParentIdWithoutCopyingTheParentGraph() {
        var model = Category.builder().id(id(17)).name("Child")
                .isExpanded(true).isFavorite(false)
                .parent(Category.builder().id(id(3)).name("Parent").build()).build();
        var dto = categories.convertToDtoList(List.of(model)).getFirst();
        assertThat(dto.getId()).isEqualTo(id(17));
        assertThat(dto.getParentId()).isEqualTo(id(3));
        assertThat(dto.getIsExpanded()).isTrue();
        assertThat(dto.getIsFavorite()).isFalse();
        var restored = categories.convertToEntity(dto);
        assertThat(restored.getParent().getId()).isEqualTo(id(3));
        assertThat(restored.getParent().getName()).isNull();
    }

    @Test
    void nullForeignKeysRemainNullReferences() {
        var category = categories.convertToEntity(new CategoryDto());
        assertThat(category.getParent()).isNull();
        var user = users.convertToEntity(new UserDto());
        assertThat(user.getSchool()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(categories.convertToDto(Category.builder().id(id(17)).build()).getParentId()).isNull();
    }

    @Test
    void distinctRelatedIdsAreMappedToTheCorrectReferences() {
        var user = users.convertToEntity(UserDto.builder().schoolId(id(19)).roleId(id(4)).build());
        assertThat(user.getSchool().getId()).isEqualTo(id(19));
        assertThat(user.getRole().getId()).isEqualTo(id(4));
    }
}
