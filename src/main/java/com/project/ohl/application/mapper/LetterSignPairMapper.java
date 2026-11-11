package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.LetterSignPair;
import com.project.ohl.application.model.LetterSignPairDto;

public final class LetterSignPairMapper {
    private LetterSignPairMapper() {
    }

    public static LetterSignPairDto convertToDto(LetterSignPair model) {
        return LetterSignPairDto.builder().id(model.getId())
              .letter(model.getLetter())
              .imageId(model.getImageId())
              .build();
    }

    public static LetterSignPair convertToEntity(LetterSignPairDto dto) {
        return LetterSignPair.builder().id(dto.getId())
              .letter(dto.getLetter())
              .imageId(dto.getImageId())
              .build();
    }
}
