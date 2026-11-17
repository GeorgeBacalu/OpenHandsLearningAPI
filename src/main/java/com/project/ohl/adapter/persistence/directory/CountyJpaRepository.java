package com.project.ohl.adapter.persistence.directory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountyJpaRepository extends JpaRepository<CountyEntity, UUID>, DirectoryImportOperations {
    List<CountyEntity> findAllByOrderByNameAscIdAsc();
}
