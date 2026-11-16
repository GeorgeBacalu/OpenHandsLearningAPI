package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    List<User> findAll();

    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);

    User save(User user);

    User update(User user);

    void delete(User user);
}
