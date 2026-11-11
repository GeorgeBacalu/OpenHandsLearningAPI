package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Classroom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassroomRepositoryPort {
    List<Classroom> findAll();
    Optional<Classroom> findById(UUID id);
    Classroom save(Classroom model);
    Classroom update(Classroom model);
    void delete(Classroom model);
}
