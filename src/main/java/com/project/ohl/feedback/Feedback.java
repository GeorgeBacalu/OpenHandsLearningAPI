package com.project.ohl.feedback;

import com.project.ohl.user.User;
import com.project.ohl.feedback.enums.FeedbackType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Feedback {

    private Long id;
    private FeedbackType type;
    private String description;
    private LocalDateTime sentAt;
    private User user;
}
