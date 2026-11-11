package com.project.llt.feedback;

import com.project.llt.exception.ResourceNotFoundException;
import com.project.llt.user.UserService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.project.llt.constants.ExceptionMessageConstants.FEEDBACK_NOT_FOUND;
import static com.project.llt.mapper.FeedbackMapper.convertToDto;
import static com.project.llt.mapper.FeedbackMapper.convertToEntity;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackDao feedbackDao;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final Clock clock;

    @Override
    public List<FeedbackDto> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackDao.findAll();
        return !feedbacks.isEmpty() ? feedbacks.stream().map(feedback -> convertToDto(modelMapper, feedback)).toList() : new ArrayList<>();
    }

    @Override
    public FeedbackDto getFeedbackById(Long id) {
        Feedback feedback = getFeedbackEntityById(id);
        return convertToDto(modelMapper, feedback);
    }

    @Override
    public FeedbackDto saveFeedback(FeedbackDto feedbackDto) {
        Feedback feedback = convertToEntity(modelMapper, feedbackDto);
        feedback.setSentAt(LocalDateTime.now(clock));
        Feedback savedFeedback = feedbackDao.save(feedback);
        return convertToDto(modelMapper, savedFeedback);
    }

    @Override
    public FeedbackDto updateFeedbackById(FeedbackDto feedbackDto, Long id) {
        Feedback feedbackToUpdate = getFeedbackEntityById(id);
        feedbackToUpdate.setType(feedbackDto.getType());
        feedbackToUpdate.setDescription(feedbackDto.getDescription());
        feedbackToUpdate.setUser(userService.getUserEntityById(feedbackDto.getUserId()));
        Feedback updatedFeedback = feedbackDao.update(feedbackToUpdate);
        return convertToDto(modelMapper, updatedFeedback);
    }

    @Override
    public void deleteFeedbackById(Long id) {
        Feedback feedbackToDelete = getFeedbackEntityById(id);
        feedbackDao.delete(feedbackToDelete);
    }

    private Feedback getFeedbackEntityById(Long id) {
        return feedbackDao.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(FEEDBACK_NOT_FOUND, id)));
    }
}
