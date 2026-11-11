package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.LetterSignPair;
import com.project.ohl.application.model.LetterSignPairDto;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfiguration.class)
public interface LetterSignPairMapper {
    LetterSignPairDto convertToDto(LetterSignPair model);

    LetterSignPair convertToEntity(LetterSignPairDto dto);
}
