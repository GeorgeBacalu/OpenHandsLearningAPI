package com.project.ohl.application.model;

import com.project.ohl.domain.model.Role;
import com.project.ohl.domain.model.User;
import java.io.Serializable;
import java.util.UUID;

public record Account(UUID id, String name, String email, String phone, Role role, UUID schoolId,
        String schoolName, String cityName, String countyName) implements Serializable {

    public Account(UUID id, String name, String email, String phone, Role role, UUID schoolId) {
        this(id, name, email, phone, role, schoolId, null, null, null);
    }

    public static Account from(User user) {
        return new Account(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRole(), user.getSchool().getId(), user.getSchool().getName(),
                user.getSchool().getCityName(), user.getSchool().getCountyName());
    }
}
