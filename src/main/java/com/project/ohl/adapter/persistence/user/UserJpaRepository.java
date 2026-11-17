package com.project.ohl.adapter.persistence.user;

import com.project.ohl.adapter.persistence.school.SchoolEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update UserEntity u set u.school = :replacement where u.school.id = :oldId")
    int reassignSchool(UUID oldId, SchoolEntity replacement);
}
