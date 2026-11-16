package com.project.ohl.mapper;

import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.mapper.UserMapper;
import com.project.ohl.application.model.UserDto;
import com.project.ohl.domain.model.Category;
import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.User;
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
                .parent(Category.builder().id(id(3)).name("Parent").build()).build();
        var dto = categories.convertToDto(model);

        assertThat(dto.getId()).isEqualTo(id(17));
        assertThat(dto.getParentId()).isEqualTo(id(3));
        assertThat(dto.getIsFavorite()).isFalse();

    }

    @Test
    void nullForeignKeysRemainNullReferences() {
        var user = users.convertToEntity(new UserDto());

        assertThat(user.getSchool()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(categories.convertToDto(Category.builder().id(id(17)).build()).getParentId()).isNull();
    }

    @Test
    void mapsSchoolReferenceAndRoleEnum() {
        var user = users.convertToEntity(UserDto.builder().schoolId(id(19)).role(Role.ADMIN).build());

        assertThat(user.getSchool().getId()).isEqualTo(id(19));
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void passwordHashesNeverMapToRawPasswordFields() {
        var stored = User.builder().passwordHash("stored-hash").build();
        var request = UserDto.builder().password("raw-request-password").build();

        assertThat(users.convertToDto(stored).getPassword()).isNull();
        assertThat(users.convertToEntity(request).getPasswordHash()).isNull();
    }
}
