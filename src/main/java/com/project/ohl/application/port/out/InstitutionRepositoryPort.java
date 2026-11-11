package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Institution;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepositoryPort {

    List<Institution> findAll();

    Optional<Institution> findById(Long id);

    Institution save(Institution institution);

    Institution update(Institution institution);

    void delete(Institution institution);
}
