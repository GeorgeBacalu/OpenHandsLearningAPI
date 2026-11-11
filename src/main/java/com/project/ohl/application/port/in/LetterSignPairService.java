package com.project.ohl.application.port.in;

import java.util.UUID;
import com.project.ohl.application.model.LetterSignPairDto;

import java.util.List;

public interface LetterSignPairService {

    List<LetterSignPairDto> getAllLetterSignPairs();

    LetterSignPairDto getLetterSignPairById(UUID id);

    LetterSignPairDto saveLetterSignPair(LetterSignPairDto letterSignPairDto);

    LetterSignPairDto updateLetterSignPairById(LetterSignPairDto letterSignPairDto, UUID id);

    void deleteLetterSignPairById(UUID id);
}
