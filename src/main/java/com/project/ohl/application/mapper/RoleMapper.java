package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Role;
import com.project.ohl.application.model.RoleDto;

public final class RoleMapper {
    private RoleMapper() {
    }

    public static RoleDto convertToDto(Role model) {
        return RoleDto.builder().id(model.getId())
              .authority(model.getAuthority())
              .build();
    }

    public static Role convertToEntity(RoleDto dto) {
        return Role.builder().id(dto.getId())
              .authority(dto.getAuthority())
              .build();
    }
}
