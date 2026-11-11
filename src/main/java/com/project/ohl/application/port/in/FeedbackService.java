package com.project.ohl.application.port.in;

import com.project.ohl.application.model.FeedbackDto;
import java.util.List;
import java.util.UUID;

public interface FeedbackService {

    List<FeedbackDto> getAllFeedbacks(UUID userId, int page, int size);

    FeedbackDto getFeedbackById(UUID id);

    FeedbackDto saveFeedback(FeedbackDto feedbackDto);

    FeedbackDto updateFeedbackById(FeedbackDto feedbackDto, UUID id);

    void deleteFeedbackById(UUID id);
}
