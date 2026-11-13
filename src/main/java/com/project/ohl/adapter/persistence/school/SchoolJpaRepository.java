package com.project.ohl.adapter.persistence.school;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolJpaRepository extends JpaRepository<SchoolEntity, UUID> {
}
