package com.project.llt.feedback;

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
import static com.project.llt.mapper.FeedbackMapper.convertToDto;
import static com.project.llt.mock.FeedbackMock.getMockedFeedback1;
import static com.project.llt.mock.FeedbackMock.getMockedFeedback2;
import static com.project.llt.mock.FeedbackMock.getMockedFeedbacks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    @InjectMocks
    private FeedbackController feedbackController;

    @Mock
    private FeedbackService feedbackService;

    @Spy
    private ModelMapper modelMapper;

    private FeedbackDto feedbackDto1;
    private FeedbackDto feedbackDto2;
    private List<FeedbackDto> feedbackDtos;

    @BeforeEach
    void setUp() {
        feedbackDto1 = convertToDto(modelMapper, getMockedFeedback1());
        feedbackDto2 = convertToDto(modelMapper, getMockedFeedback2());
        feedbackDtos = getMockedFeedbacks().stream().map(Feedback -> convertToDto(modelMapper, Feedback)).toList();
    }

    @Test
    void getAllFeedbacks_shouldReturnListOfFeedbacks() {
        given(feedbackService.getAllFeedbacks()).willReturn(feedbackDtos);
        ResponseEntity<List<FeedbackDto>> response = feedbackController.getAllFeedbacks();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(feedbackDtos);
    }

    @Test
    void getFeedbackById_shouldReturnFeedbackWithGivenId() {
        given(feedbackService.getFeedbackById(anyLong())).willReturn(feedbackDto1);
        ResponseEntity<FeedbackDto> response = feedbackController.getFeedbackById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(feedbackDto1);
    }

    @Test
    void saveFeedback_shouldAddFeedbackToList() {
        given(feedbackService.saveFeedback(any(FeedbackDto.class))).willReturn(feedbackDto1);
        ResponseEntity<FeedbackDto> response = feedbackController.saveFeedback(feedbackDto1);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(feedbackDto1);
    }

    @Test
    void updateFeedbackById_shouldUpdateFeedbackWithGivenId() {
        FeedbackDto feedbackDto = feedbackDto2; feedbackDto.setId(VALID_ID);
        given(feedbackService.updateFeedbackById(any(FeedbackDto.class), anyLong())).willReturn(feedbackDto);
        ResponseEntity<FeedbackDto> response = feedbackController.updateFeedbackById(feedbackDto2, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(feedbackDto);
    }

    @Test
    void deleteFeedbackById_shouldRemoveFeedbackWithGivenIdFromList() {
        ResponseEntity<Void> response = feedbackController.deleteFeedbackById(VALID_ID);
        verify(feedbackService).deleteFeedbackById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
