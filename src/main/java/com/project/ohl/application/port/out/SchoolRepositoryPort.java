package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.School;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRepositoryPort {

    List<School> findAll();

    Optional<School> findById(UUID id);

    School save(School school);

    School update(School school);

    void delete(School school);
}
