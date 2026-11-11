package com.project.llt.feedback;

import com.project.llt.exception.ResourceNotFoundException;
import com.project.llt.user.User;
import com.project.llt.user.UserService;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

import static com.project.llt.constants.ExceptionMessageConstants.FEEDBACK_NOT_FOUND;
import static com.project.llt.constants.IdentifierConstants.INVALID_ID;
import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.FeedbackMapper.convertToDto;
import static com.project.llt.mock.FeedbackMock.getMockedFeedback1;
import static com.project.llt.mock.FeedbackMock.getMockedFeedback2;
import static com.project.llt.mock.FeedbackMock.getMockedFeedbacks;
import static com.project.llt.mock.UserMock.getMockedUser2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    @Mock
    private FeedbackDao feedbackDao;

    @Mock
    private UserService userService;

    @Spy
    private ModelMapper modelMapper;

    @Mock
    private Clock clock;

    private static final ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Bucharest"));

    @Captor
    private ArgumentCaptor<Feedback> feedbackCaptor;

    private Feedback feedback1;
    private Feedback feedback2;
    private List<Feedback> feedbacks;
    private User user;
    private FeedbackDto feedbackDto1;
    private FeedbackDto feedbackDto2;
    private List<FeedbackDto> feedbackDtos;

    @BeforeEach
    void setUp() {
        feedback1 = getMockedFeedback1();
        feedback2 = getMockedFeedback2();
        feedbacks = getMockedFeedbacks();
        user = getMockedUser2();
        feedbackDto1 = convertToDto(modelMapper, feedback1);
        feedbackDto2 = convertToDto(modelMapper, feedback2);
        feedbackDtos = feedbacks.stream().map(feedback -> convertToDto(modelMapper, feedback)).toList();
    }

    @Test
    void getAllFeedbacks_shouldReturnListOfFeedbacks() {
        given(feedbackDao.findAll()).willReturn(feedbacks);
        List<FeedbackDto> result = feedbackService.getAllFeedbacks();
        assertThat(result).isEqualTo(feedbackDtos);
    }

    @Test
    void getFeedbackById_withValidId_shouldReturnFeedbackWithGivenId() {
        given(feedbackDao.findById(anyLong())).willReturn(Optional.ofNullable(feedback1));
        FeedbackDto result = feedbackService.getFeedbackById(VALID_ID);
        assertThat(result).isEqualTo(feedbackDto1);
    }

    @Test
    void getFeedbackById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> feedbackService.getFeedbackById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(FEEDBACK_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveFeedback_shouldAddFeedbackToList() {
        given(clock.getZone()).willReturn(zonedDateTime.getZone());
        given(clock.instant()).willReturn(zonedDateTime.toInstant());
        feedback1.setSentAt(zonedDateTime.toLocalDateTime());
        given(feedbackDao.save(any(Feedback.class))).willReturn(feedback1);
        FeedbackDto result = feedbackService.saveFeedback(feedbackDto1);
        verify(feedbackDao).save(feedbackCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, feedbackCaptor.getValue()));
    }

    @Test
    void updateFeedbackById_withValidId_shouldUpdateFeedbackWithGivenId() {
        Feedback feedback = feedback2; feedback.setId(VALID_ID);
        feedback.setSentAt(feedback1.getSentAt());
        given(feedbackDao.findById(anyLong())).willReturn(Optional.ofNullable(feedback1));
        given(userService.getUserEntityById(anyLong())).willReturn(user);
        given(feedbackDao.update(any(Feedback.class))).willReturn(feedback);
        FeedbackDto result = feedbackService.updateFeedbackById(feedbackDto2, VALID_ID);
        verify(feedbackDao).update(feedbackCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, feedbackCaptor.getValue()));
    }

    @Test
    void updateFeedbackById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> feedbackService.updateFeedbackById(feedbackDto2, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(FEEDBACK_NOT_FOUND, INVALID_ID));
        verify(feedbackDao, never()).update(any(Feedback.class));
    }

    @Test
    void deleteFeedbackById_withValidId_shouldRemoveFeedbackWithGivenIdFromList() {
        given(feedbackDao.findById(anyLong())).willReturn(Optional.ofNullable(feedback1));
        feedbackService.deleteFeedbackById(VALID_ID);
        verify(feedbackDao).delete(feedback1);
    }

    @Test
    void deleteFeedbackById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> feedbackService.deleteFeedbackById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(FEEDBACK_NOT_FOUND, INVALID_ID));
        verify(feedbackDao, never()).delete(any(Feedback.class));
    }
}
