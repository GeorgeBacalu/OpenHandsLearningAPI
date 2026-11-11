package com.project.llt.mapper;

import com.project.llt.role.Role;
import com.project.llt.role.RoleDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleMapper {

    public static RoleDto convertToDto(ModelMapper modelMapper, Role role) {
        return modelMapper.map(role, RoleDto.class);
    }

    public static Role convertToEntity(ModelMapper modelMapper, RoleDto roleDto) {
        return modelMapper.map(roleDto, Role.class);
    }
}
