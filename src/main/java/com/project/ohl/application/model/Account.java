package com.project.ohl.application.model;

import com.project.ohl.domain.model.Authority;
import com.project.ohl.domain.model.User;
import java.io.Serializable;
import java.util.UUID;

public record Account(UUID id, String name, String email, String phone, UUID roleId, UUID schoolId,
        Authority authority) implements Serializable {
    public static Account from(User user) {
        return new Account(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRole().getId(), user.getSchool().getId(), user.getRole().getAuthority());
    }
}
