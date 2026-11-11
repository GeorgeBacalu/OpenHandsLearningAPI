package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Institution;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepositoryPort {

    List<Institution> findAll();

    Optional<Institution> findById(UUID id);

    Institution save(Institution institution);

    Institution update(Institution institution);

    void delete(Institution institution);
}
