package com.project.ohl.application.service;

import com.project.ohl.application.model.LetterSignPairDto;
import com.project.ohl.application.port.in.LetterSignPairService;
import com.project.ohl.application.port.out.LetterSignPairRepositoryPort;
import com.project.ohl.domain.model.LetterSignPair;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.LETTER_SIGN_PAIR_NOT_FOUND;
import static com.project.ohl.application.mapper.LetterSignPairMapper.convertToDto;
import static com.project.ohl.application.mapper.LetterSignPairMapper.convertToEntity;

@RequiredArgsConstructor
public class LetterSignPairServiceImpl implements LetterSignPairService {

    private final LetterSignPairRepositoryPort letterSignPairRepository;

    @Override
    public List<LetterSignPairDto> getAllLetterSignPairs() {
        List<LetterSignPair> letterSignPairs = letterSignPairRepository.findAll();
        return letterSignPairs.stream().map(letterSignPair -> convertToDto(letterSignPair)).toList();
    }

    @Override
    public LetterSignPairDto getLetterSignPairById(Long id) {
        LetterSignPair letterSignPair = getLetterSignPairEntityById(id);
        return convertToDto(letterSignPair);
    }

    @Override
    public LetterSignPairDto saveLetterSignPair(LetterSignPairDto letterSignPairDto) {
        LetterSignPair letterSignPair = convertToEntity(letterSignPairDto);
        LetterSignPair savedLetterSignPair = letterSignPairRepository.save(letterSignPair);
        return convertToDto(savedLetterSignPair);
    }

    @Override
    public LetterSignPairDto updateLetterSignPairById(LetterSignPairDto letterSignPairDto, Long id) {
        LetterSignPair letterSignPairToUpdate = getLetterSignPairEntityById(id);
        letterSignPairToUpdate.setLetter(letterSignPairDto.getLetter());
        letterSignPairToUpdate.setImageId(letterSignPairDto.getImageId());
        LetterSignPair updatedLetterSignPair = letterSignPairRepository.update(letterSignPairToUpdate);
        return convertToDto(updatedLetterSignPair);
    }

    @Override
    public void deleteLetterSignPairById(Long id) {
        LetterSignPair letterSignPairToDelete = getLetterSignPairEntityById(id);
        letterSignPairRepository.delete(letterSignPairToDelete);
    }

    private LetterSignPair getLetterSignPairEntityById(Long id) {
        return letterSignPairRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(LETTER_SIGN_PAIR_NOT_FOUND, id)));
    }
}
