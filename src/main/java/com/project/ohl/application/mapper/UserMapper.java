package com.project.ohl.application.mapper;

import com.project.ohl.application.model.UserDto;
import com.project.ohl.domain.model.School;
import com.project.ohl.domain.model.User;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class)
public interface UserMapper {
    @Mapping(target = "schoolId", source = "school.id")
    @Mapping(target = "password", ignore = true)
    UserDto convertToDto(User model);

    @Mapping(target = "school", source = "schoolId")
    @Mapping(target = "passwordHash", ignore = true)
    User convertToEntity(UserDto dto);

    default School school(UUID id) {
        return id == null ? null : School.builder().id(id).build();
    }
}
