package com.project.ohl.application.port.in;

import com.project.ohl.application.model.LetterSignPairDto;
import java.util.List;
import java.util.UUID;

public interface LetterSignPairService {

    List<LetterSignPairDto> getAllLetterSignPairs();

    LetterSignPairDto getLetterSignPairById(UUID id);

    LetterSignPairDto saveLetterSignPair(LetterSignPairDto letterSignPairDto);

    LetterSignPairDto updateLetterSignPairById(LetterSignPairDto letterSignPairDto, UUID id);

    void deleteLetterSignPairById(UUID id);
}
