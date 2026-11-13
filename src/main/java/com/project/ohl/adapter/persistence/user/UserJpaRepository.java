package com.project.ohl.adapter.persistence.user;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    @EntityGraph(attributePaths = "role")
    Optional<UserEntity> findByEmail(String email);
}
