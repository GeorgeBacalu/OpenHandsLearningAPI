package com.project.ohl.mock;

import static com.project.ohl.TestIds.id;
import com.project.ohl.domain.model.Feedback;
import com.project.ohl.domain.model.FeedbackType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.mock.UserMock.getMockedUser1;
import static com.project.ohl.mock.UserMock.getMockedUser2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackMock {

    public static List<Feedback> getMockedFeedbacks() {
        return List.of(getMockedFeedback1(), getMockedFeedback2());
    }

    public static Feedback getMockedFeedback1() {
        return Feedback.builder()
              .id(id(1))
              .type(FeedbackType.IMPROVEMENT)
              .description("test_feedback_description1")
              .sentAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
              .user(getMockedUser1())
              .build();
    }

    public static Feedback getMockedFeedback2() {
        return Feedback.builder()
              .id(id(2))
              .type(FeedbackType.OPTIMIZATION)
              .description("test_feedback_description2")
              .sentAt(LocalDateTime.of(2000, 1, 2, 0, 0, 0))
              .user(getMockedUser2())
              .build();
    }
}
