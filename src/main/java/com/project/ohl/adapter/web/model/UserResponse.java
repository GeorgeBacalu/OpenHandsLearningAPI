package com.project.ohl.adapter.web.model;

import com.project.ohl.application.model.UserDto;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, String phone, UUID roleId, UUID schoolId) {
    public static UserResponse from(UserDto user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRoleId(), user.getSchoolId());
    }
}
