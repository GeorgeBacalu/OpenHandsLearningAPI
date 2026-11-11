package com.project.ohl.adapter.persistence.institution;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionJpaRepository extends JpaRepository<InstitutionEntity, UUID> {
}
