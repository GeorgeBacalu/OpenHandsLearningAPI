package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {

    List<Role> findAll();

    Optional<Role> findById(Long id);

    Role save(Role role);

    Role update(Role role);

    void delete(Role role);
}
