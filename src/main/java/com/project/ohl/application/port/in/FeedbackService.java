package com.project.ohl.application.port.in;

import com.project.ohl.application.model.FeedbackDto;

import java.util.List;

public interface FeedbackService {

    List<FeedbackDto> getAllFeedbacks();

    FeedbackDto getFeedbackById(Long id);

    FeedbackDto saveFeedback(FeedbackDto feedbackDto);

    FeedbackDto updateFeedbackById(FeedbackDto feedbackDto, Long id);

    void deleteFeedbackById(Long id);
}
