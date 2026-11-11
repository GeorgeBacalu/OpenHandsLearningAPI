package com.project.llt.letter_sign_pair;

import java.util.List;
import java.util.Optional;

public interface LetterSignPairDao {

    List<LetterSignPair> findAll();

    Optional<LetterSignPair> findById(Long id);

    LetterSignPair save(LetterSignPair letterSignPair);

    LetterSignPair update(LetterSignPair letterSignPair);

    void delete(LetterSignPair letterSignPair);
}
