package com.project.ohl.adapter.web.model;

import com.project.ohl.application.model.UserDto;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, String mobile, String address,
                           LocalDate birthday, UUID schoolId, UUID roleId, UUID classroomId) {
    public static UserResponse from(UserDto user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getMobile(),
              user.getAddress(), user.getBirthday(), user.getSchoolId(), user.getRoleId(), user.getClassroomId());
    }
}
