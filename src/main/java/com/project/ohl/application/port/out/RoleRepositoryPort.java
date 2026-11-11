package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepositoryPort {

    List<Role> findAll();

    Optional<Role> findById(UUID id);

    Role save(Role role);

    Role update(Role role);

    void delete(Role role);
}
