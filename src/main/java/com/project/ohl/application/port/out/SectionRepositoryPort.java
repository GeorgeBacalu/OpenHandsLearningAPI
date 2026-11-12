package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Section;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectionRepositoryPort {
    List<Section> findAll();

    Optional<Section> findById(UUID id);

    Section save(Section section);

    Section update(Section section);

    void delete(Section section);
}
