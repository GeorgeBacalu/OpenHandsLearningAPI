package com.project.ohl.adapter.persistence.classroom;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomJpaRepository extends JpaRepository<ClassroomEntity, UUID> {}
