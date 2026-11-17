package com.project.ohl.adapter.persistence.directory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CityJpaRepository extends JpaRepository<CityEntity, UUID> {
    @Query("""
            select c from CityEntity c where c.county.id = :countyId
            and lower(c.name) like :pattern escape '!'
            order by c.name, c.id limit :limit offset :offset
            """)
    List<CityEntity> search(UUID countyId, String pattern, int offset, int limit);
}
