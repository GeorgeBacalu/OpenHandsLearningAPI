package com.project.llt.letter_sign_pair;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.LetterSignPairMapper.convertToDto;
import static com.project.llt.mock.LetterSignPairMock.getMockedLetterSignPair1;
import static com.project.llt.mock.LetterSignPairMock.getMockedLetterSignPair2;
import static com.project.llt.mock.LetterSignPairMock.getMockedLetterSignPairs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LetterSignPairControllerTest {

    @InjectMocks
    private LetterSignPairController letterSignPairController;

    @Mock
    private LetterSignPairService letterSignPairService;

    @Spy
    private ModelMapper modelMapper;

    private LetterSignPairDto letterSignPairDto1;
    private LetterSignPairDto letterSignPairDto2;
    private List<LetterSignPairDto> letterSignPairDtos;

    @BeforeEach
    void setUp() {
        letterSignPairDto1 = convertToDto(modelMapper, getMockedLetterSignPair1());
        letterSignPairDto2 = convertToDto(modelMapper, getMockedLetterSignPair2());
        letterSignPairDtos = getMockedLetterSignPairs().stream().map(letterSignPair -> convertToDto(modelMapper, letterSignPair)).toList();
    }

    @Test
    void getAllLetterSignPairs_shouldReturnListOfLetterSignPairs() {
        given(letterSignPairService.getAllLetterSignPairs()).willReturn(letterSignPairDtos);
        ResponseEntity<List<LetterSignPairDto>> response = letterSignPairController.getAllLetterSignPairs();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(letterSignPairDtos);
    }

    @Test
    void getLetterSignPairById_shouldReturnLetterSignPairWithGivenId() {
        given(letterSignPairService.getLetterSignPairById(anyLong())).willReturn(letterSignPairDto1);
        ResponseEntity<LetterSignPairDto> response = letterSignPairController.getLetterSignPairById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(letterSignPairDto1);
    }

    @Test
    void saveLetterSignPair_shouldAddLetterSignPairToList() {
        given(letterSignPairService.saveLetterSignPair(any(LetterSignPairDto.class))).willReturn(letterSignPairDto1);
        ResponseEntity<LetterSignPairDto> response = letterSignPairController.saveLetterSignPair(letterSignPairDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(letterSignPairDto1);
    }

    @Test
    void updateLetterSignPairById_shouldUpdateLetterSignPairWithGivenId() {
        LetterSignPairDto letterSignPairDto = letterSignPairDto2; letterSignPairDto.setId(VALID_ID);
        given(letterSignPairService.updateLetterSignPairById(any(LetterSignPairDto.class), anyLong())).willReturn(letterSignPairDto);
        ResponseEntity<LetterSignPairDto> response = letterSignPairController.updateLetterSignPairById(letterSignPairDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(letterSignPairDto);
    }

    @Test
    void deleteLetterSignPairById_shouldRemoveLetterSignPairWithGivenIdFromList() {
        ResponseEntity<Void> response = letterSignPairController.deleteLetterSignPairById(VALID_ID);
        verify(letterSignPairService).deleteLetterSignPairById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
