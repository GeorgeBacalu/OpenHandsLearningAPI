package com.project.ohl.adapter.persistence.section;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionJpaRepository extends JpaRepository<SectionEntity, UUID> {
}
