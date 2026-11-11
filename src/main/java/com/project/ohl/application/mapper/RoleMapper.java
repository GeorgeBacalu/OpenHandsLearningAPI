package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Role;
import com.project.ohl.application.model.RoleDto;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfiguration.class)
public interface RoleMapper {
    RoleDto convertToDto(Role model);

    Role convertToEntity(RoleDto dto);
}
