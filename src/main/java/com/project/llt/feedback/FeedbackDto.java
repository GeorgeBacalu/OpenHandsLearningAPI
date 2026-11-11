package com.project.llt.feedback;

import com.project.llt.feedback.enums.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackDto {

    @Positive(message = "Feedback ID must be positive")
    private Long id;

    @NotNull(message = "Feedback type must not be null")
    private FeedbackType type;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sentAt;

    @NotNull(message = "User ID must not be null")
    @Positive(message = "User ID must be positive")
    private Long userId;
}
