package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.LetterSignPair;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LetterSignPairRepositoryPort {

    List<LetterSignPair> findAll();

    Optional<LetterSignPair> findById(UUID id);

    LetterSignPair save(LetterSignPair letterSignPair);

    LetterSignPair update(LetterSignPair letterSignPair);

    void delete(LetterSignPair letterSignPair);
}
