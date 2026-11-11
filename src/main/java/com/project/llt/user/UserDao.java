package com.project.llt.user;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    List<User> findAll();

    Optional<User> findById(Long id);

    User save(User user);

    User update(User user);

    void delete(User user);
}
