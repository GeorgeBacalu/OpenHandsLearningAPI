package com.project.llt.role;

import java.util.List;
import java.util.Optional;

public interface RoleDao {

    List<Role> findAll();

    Optional<Role> findById(Long id);

    Role save(Role role);

    Role update(Role role);

    void delete(Role role);
}
