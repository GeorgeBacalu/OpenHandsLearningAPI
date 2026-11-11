package com.project.ohl.application.port.in;

import java.util.UUID;
import com.project.ohl.application.model.FeedbackDto;

import java.util.List;

public interface FeedbackService {

    List<FeedbackDto> getAllFeedbacks();

    FeedbackDto getFeedbackById(UUID id);

    FeedbackDto saveFeedback(FeedbackDto feedbackDto);

    FeedbackDto updateFeedbackById(FeedbackDto feedbackDto, UUID id);

    void deleteFeedbackById(UUID id);
}
