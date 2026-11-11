package com.project.ohl.application.port.out;

import java.util.UUID;
import com.project.ohl.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    List<User> findAll();

    Optional<User> findById(UUID id);

    User save(User user);

    User update(User user);

    void delete(User user);
}
