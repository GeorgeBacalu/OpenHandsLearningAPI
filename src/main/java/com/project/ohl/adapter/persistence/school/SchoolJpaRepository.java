package com.project.ohl.adapter.persistence.school;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SchoolJpaRepository extends JpaRepository<SchoolEntity, UUID> {
    @Query("""
            select s from SchoolEntity s where s.city.id = :cityId
            and lower(s.name) like :pattern escape '!'
            order by s.name, s.id limit :limit offset :offset
            """)
    List<SchoolEntity> search(UUID cityId, String pattern, int offset, int limit);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SchoolEntity s where s.id = :id")
    Optional<SchoolEntity> findByIdForUpdate(UUID id);
}
