package com.project.llt.letter_sign_pair;

import com.project.llt.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.project.llt.constants.ExceptionMessageConstants.LETTER_SIGN_PAIR_NOT_FOUND;
import static com.project.llt.mapper.LetterSignPairMapper.convertToDto;
import static com.project.llt.mapper.LetterSignPairMapper.convertToEntity;

@Service
@RequiredArgsConstructor
public class LetterSignPairServiceImpl implements LetterSignPairService {

    private final LetterSignPairDao letterSignPairDao;
    private final ModelMapper modelMapper;

    @Override
    public List<LetterSignPairDto> getAllLetterSignPairs() {
        List<LetterSignPair> letterSignPairs = letterSignPairDao.findAll();
        return !letterSignPairs.isEmpty() ? letterSignPairs.stream().map(letterSignPair -> convertToDto(modelMapper, letterSignPair)).toList() : new ArrayList<>();
    }

    @Override
    public LetterSignPairDto getLetterSignPairById(Long id) {
        LetterSignPair letterSignPair = getLetterSignPairEntityById(id);
        return convertToDto(modelMapper, letterSignPair);
    }

    @Override
    public LetterSignPairDto saveLetterSignPair(LetterSignPairDto letterSignPairDto) {
        LetterSignPair letterSignPair = convertToEntity(modelMapper, letterSignPairDto);
        LetterSignPair savedLetterSignPair = letterSignPairDao.save(letterSignPair);
        return convertToDto(modelMapper, savedLetterSignPair);
    }

    @Override
    public LetterSignPairDto updateLetterSignPairById(LetterSignPairDto letterSignPairDto, Long id) {
        LetterSignPair letterSignPairToUpdate = getLetterSignPairEntityById(id);
        letterSignPairToUpdate.setLetter(letterSignPairDto.getLetter());
        letterSignPairToUpdate.setImageId(letterSignPairDto.getImageId());
        LetterSignPair updatedLetterSignPair = letterSignPairDao.update(letterSignPairToUpdate);
        return convertToDto(modelMapper, updatedLetterSignPair);
    }

    @Override
    public void deleteLetterSignPairById(Long id) {
        LetterSignPair letterSignPairToDelete = getLetterSignPairEntityById(id);
        letterSignPairDao.delete(letterSignPairToDelete);
    }

    private LetterSignPair getLetterSignPairEntityById(Long id) {
        return letterSignPairDao.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(LETTER_SIGN_PAIR_NOT_FOUND, id)));
    }
}
