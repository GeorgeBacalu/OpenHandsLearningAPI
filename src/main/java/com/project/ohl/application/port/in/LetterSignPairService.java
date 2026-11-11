package com.project.ohl.application.port.in;

import com.project.ohl.application.model.LetterSignPairDto;

import java.util.List;

public interface LetterSignPairService {

    List<LetterSignPairDto> getAllLetterSignPairs();

    LetterSignPairDto getLetterSignPairById(Long id);

    LetterSignPairDto saveLetterSignPair(LetterSignPairDto letterSignPairDto);

    LetterSignPairDto updateLetterSignPairById(LetterSignPairDto letterSignPairDto, Long id);

    void deleteLetterSignPairById(Long id);
}
