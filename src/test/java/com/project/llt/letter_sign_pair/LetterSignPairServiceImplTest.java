package com.project.llt.letter_sign_pair;

import com.project.llt.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static com.project.llt.constants.ExceptionMessageConstants.LETTER_SIGN_PAIR_NOT_FOUND;
import static com.project.llt.constants.IdentifierConstants.INVALID_ID;
import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.LetterSignPairMapper.convertToDto;
import static com.project.llt.mock.LetterSignPairMock.getMockedLetterSignPair1;
import static com.project.llt.mock.LetterSignPairMock.getMockedLetterSignPair2;
import static com.project.llt.mock.LetterSignPairMock.getMockedLetterSignPairs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LetterSignPairServiceImplTest {

    @InjectMocks
    private LetterSignPairServiceImpl letterSignPairService;

    @Mock
    private LetterSignPairDao letterSignPairDao;

    @Spy
    private ModelMapper modelMapper;

    @Captor
    private ArgumentCaptor<LetterSignPair> letterSignPairCaptor;

    private LetterSignPair letterSignPair1;
    private LetterSignPair letterSignPair2;
    private List<LetterSignPair> letterSignPairs;
    private LetterSignPairDto letterSignPairDto1;
    private LetterSignPairDto letterSignPairDto2;
    private List<LetterSignPairDto> letterSignPairDtos;

    @BeforeEach
    void setUp() {
        letterSignPair1 = getMockedLetterSignPair1();
        letterSignPair2 = getMockedLetterSignPair2();
        letterSignPairs = getMockedLetterSignPairs();
        letterSignPairDto1 = convertToDto(modelMapper, letterSignPair1);
        letterSignPairDto2 = convertToDto(modelMapper, letterSignPair2);
        letterSignPairDtos = letterSignPairs.stream().map(letterSignPair -> convertToDto(modelMapper, letterSignPair)).toList();
    }

    @Test
    void getAllLetterSignPairs_shouldReturnListOfLetterSignPairs() {
        given(letterSignPairDao.findAll()).willReturn(letterSignPairs);
        List<LetterSignPairDto> result = letterSignPairService.getAllLetterSignPairs();
        assertThat(result).isEqualTo(letterSignPairDtos);
    }

    @Test
    void getLetterSignPairById_withValidId_shouldReturnLetterSignPairWithGivenId() {
        given(letterSignPairDao.findById(anyLong())).willReturn(Optional.ofNullable(letterSignPair1));
        LetterSignPairDto result = letterSignPairService.getLetterSignPairById(VALID_ID);
        assertThat(result).isEqualTo(letterSignPairDto1);
    }

    @Test
    void getLetterSignPairById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> letterSignPairService.getLetterSignPairById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(LETTER_SIGN_PAIR_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveLetterSignPair_shouldAddLetterSignPairToList() {
        given(letterSignPairDao.save(any(LetterSignPair.class))).willReturn(letterSignPair1);
        LetterSignPairDto result = letterSignPairService.saveLetterSignPair(letterSignPairDto1);
        verify(letterSignPairDao).save(letterSignPairCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, letterSignPairCaptor.getValue()));
    }

    @Test
    void updateLetterSignPairById_withValidId_shouldUpdateLetterSignPairWithGivenId() {
        LetterSignPair letterSignPair = letterSignPair2; letterSignPair.setId(VALID_ID);
        given(letterSignPairDao.findById(anyLong())).willReturn(Optional.ofNullable(letterSignPair1));
        given(letterSignPairDao.update(any(LetterSignPair.class))).willReturn(letterSignPair);
        LetterSignPairDto result = letterSignPairService.updateLetterSignPairById(letterSignPairDto2, VALID_ID);
        verify(letterSignPairDao).update(letterSignPairCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, letterSignPairCaptor.getValue()));
    }

    @Test
    void updateLetterSignPairById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> letterSignPairService.updateLetterSignPairById(letterSignPairDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(LETTER_SIGN_PAIR_NOT_FOUND, INVALID_ID));
        verify(letterSignPairDao, never()).update(any(LetterSignPair.class));
    }

    @Test
    void deleteLetterSignPairById_withValidId_shouldRemoveLetterSignPairWithGivenIdFromList() {
        given(letterSignPairDao.findById(anyLong())).willReturn(Optional.ofNullable(letterSignPair1));
        letterSignPairService.deleteLetterSignPairById(VALID_ID);
        verify(letterSignPairDao).delete(letterSignPair1);
    }

    @Test
    void deleteLetterSignPairById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> letterSignPairService.deleteLetterSignPairById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(LETTER_SIGN_PAIR_NOT_FOUND, INVALID_ID));
        verify(letterSignPairDao, never()).delete(any(LetterSignPair.class));
    }
}
