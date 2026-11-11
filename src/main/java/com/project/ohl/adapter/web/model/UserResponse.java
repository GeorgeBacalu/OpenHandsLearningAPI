package com.project.ohl.adapter.web.model;

import com.project.ohl.application.model.UserDto;
import java.time.LocalDate;

public record UserResponse(Long id, String name, String email, String mobile, String address,
                           LocalDate birthday, Long institutionId, Long roleId) {
    public static UserResponse from(UserDto user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getMobile(),
              user.getAddress(), user.getBirthday(), user.getInstitutionId(), user.getRoleId());
    }
}
