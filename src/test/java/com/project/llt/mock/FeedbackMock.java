package com.project.llt.mock;

import com.project.llt.feedback.Feedback;
import com.project.llt.feedback.enums.FeedbackType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.llt.mock.UserMock.getMockedUser1;
import static com.project.llt.mock.UserMock.getMockedUser2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackMock {

    public static List<Feedback> getMockedFeedbacks() {
        return List.of(getMockedFeedback1(), getMockedFeedback2());
    }

    public static Feedback getMockedFeedback1() {
        return Feedback.builder()
              .id(1L)
              .type(FeedbackType.IMPROVEMENT)
              .description("test_feedback_description1")
              .sentAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
              .user(getMockedUser1())
              .build();
    }

    public static Feedback getMockedFeedback2() {
        return Feedback.builder()
              .id(2L)
              .type(FeedbackType.OPTIMIZATION)
              .description("test_feedback_description2")
              .sentAt(LocalDateTime.of(2000, 1, 2, 0, 0, 0))
              .user(getMockedUser2())
              .build();
    }
}
