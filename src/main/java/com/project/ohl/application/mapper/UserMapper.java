package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.User;
import com.project.ohl.application.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class, uses = ReferenceMapper.class)
public interface UserMapper {
    @Mapping(target = "institutionId", source = "institution.id")
    @Mapping(target = "roleId", source = "role.id")
    UserDto convertToDto(User model);

    @Mapping(target = "institution", source = "institutionId")
    @Mapping(target = "role", source = "roleId")
    User convertToEntity(UserDto dto);
}
