package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.LetterSignPair;

import java.util.List;
import java.util.Optional;

public interface LetterSignPairRepositoryPort {

    List<LetterSignPair> findAll();

    Optional<LetterSignPair> findById(Long id);

    LetterSignPair save(LetterSignPair letterSignPair);

    LetterSignPair update(LetterSignPair letterSignPair);

    void delete(LetterSignPair letterSignPair);
}
