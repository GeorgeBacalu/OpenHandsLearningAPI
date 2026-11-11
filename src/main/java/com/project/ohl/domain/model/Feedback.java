package com.project.ohl.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Feedback {

    private UUID id;
    private FeedbackType type;
    private String description;
    private LocalDateTime sentAt;
    private User user;
}
