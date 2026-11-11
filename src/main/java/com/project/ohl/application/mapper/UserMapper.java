package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Institution;
import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.User;
import com.project.ohl.application.model.UserDto;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDto convertToDto(User model) {
        return UserDto.builder().id(model.getId())
              .name(model.getName())
              .email(model.getEmail())
              .password(model.getPassword())
              .mobile(model.getMobile())
              .address(model.getAddress())
              .birthday(model.getBirthday())
              .institutionId(model.getInstitution() == null ? null : model.getInstitution().getId())
              .roleId(model.getRole() == null ? null : model.getRole().getId())
              .build();
    }

    public static User convertToEntity(UserDto dto) {
        return User.builder().id(dto.getId())
              .name(dto.getName())
              .email(dto.getEmail())
              .password(dto.getPassword())
              .mobile(dto.getMobile())
              .address(dto.getAddress())
              .birthday(dto.getBirthday())
              .institution(dto.getInstitutionId() == null ? null : Institution.builder().id(dto.getInstitutionId()).build())
              .role(dto.getRoleId() == null ? null : Role.builder().id(dto.getRoleId()).build())
              .build();
    }
}
