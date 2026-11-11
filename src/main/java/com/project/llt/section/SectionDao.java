package com.project.llt.section;

import java.util.List;
import java.util.Optional;

public interface SectionDao {

    List<Section> findAll();

    Optional<Section> findById(Long id);

    Section save(Section section);

    Section update(Section section);

    void delete(Section section);
}
