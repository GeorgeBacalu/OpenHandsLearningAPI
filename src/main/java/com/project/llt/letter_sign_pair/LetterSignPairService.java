package com.project.llt.letter_sign_pair;

import java.util.List;

public interface LetterSignPairService {

    List<LetterSignPairDto> getAllLetterSignPairs();

    LetterSignPairDto getLetterSignPairById(Long id);

    LetterSignPairDto saveLetterSignPair(LetterSignPairDto letterSignPairDto);

    LetterSignPairDto updateLetterSignPairById(LetterSignPairDto letterSignPairDto, Long id);

    void deleteLetterSignPairById(Long id);
}
