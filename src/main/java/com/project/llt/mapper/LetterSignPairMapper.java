package com.project.llt.mapper;

import com.project.llt.letter_sign_pair.LetterSignPair;
import com.project.llt.letter_sign_pair.LetterSignPairDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LetterSignPairMapper {

    public static LetterSignPairDto convertToDto(ModelMapper modelMapper, LetterSignPair letterSignPair) {
        return modelMapper.map(letterSignPair, LetterSignPairDto.class);
    }

    public static LetterSignPair convertToEntity(ModelMapper modelMapper, LetterSignPairDto letterSignPairDto) {
        return modelMapper.map(letterSignPairDto, LetterSignPair.class);
    }
}
