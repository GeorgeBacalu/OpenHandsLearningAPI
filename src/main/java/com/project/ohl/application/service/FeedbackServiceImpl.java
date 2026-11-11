package com.project.ohl.application.service;

import com.project.ohl.application.mapper.FeedbackMapper;
import com.project.ohl.application.model.FeedbackDto;
import com.project.ohl.application.port.in.FeedbackService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.FeedbackRepositoryPort;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Feedback;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.FEEDBACK_NOT_FOUND;

@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepositoryPort feedbackRepository;
    private final UserService userService;
    private final Clock clock;

    private final FeedbackMapper mapper;

    @Override
    public List<FeedbackDto> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return feedbacks.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public FeedbackDto getFeedbackById(UUID id) {
        Feedback feedback = getFeedbackEntityById(id);
        return mapper.convertToDto(feedback);
    }

    @Override
    public FeedbackDto saveFeedback(FeedbackDto feedbackDto) {
        Feedback feedback = mapper.convertToEntity(feedbackDto);
        feedback.setSentAt(LocalDateTime.now(clock));
        Feedback savedFeedback = feedbackRepository.save(feedback);
        return mapper.convertToDto(savedFeedback);
    }

    @Override
    public FeedbackDto updateFeedbackById(FeedbackDto feedbackDto, UUID id) {
        Feedback feedbackToUpdate = getFeedbackEntityById(id);
        feedbackToUpdate.setType(feedbackDto.getType());
        feedbackToUpdate.setDescription(feedbackDto.getDescription());
        feedbackToUpdate.setUser(userService.getUserEntityById(feedbackDto.getUserId()));
        Feedback updatedFeedback = feedbackRepository.update(feedbackToUpdate);
        return mapper.convertToDto(updatedFeedback);
    }

    @Override
    public void deleteFeedbackById(UUID id) {
        Feedback feedbackToDelete = getFeedbackEntityById(id);
        feedbackRepository.delete(feedbackToDelete);
    }

    private Feedback getFeedbackEntityById(UUID id) {
        return feedbackRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(FEEDBACK_NOT_FOUND, id)));
    }
}
