package com.project.llt.feedback;

import java.util.List;

public interface FeedbackService {
    
    List<FeedbackDto> getAllFeedbacks();

    FeedbackDto getFeedbackById(Long id);

    FeedbackDto saveFeedback(FeedbackDto feedbackDto);

    FeedbackDto updateFeedbackById(FeedbackDto feedbackDto, Long id);
    
    void deleteFeedbackById(Long id);
}
