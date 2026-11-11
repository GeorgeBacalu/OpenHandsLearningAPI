package com.project.ohl.application.model;

import com.project.ohl.domain.model.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class FeedbackDto {

    private UUID id;

    @NotNull(message = "Feedback type must not be null")
    private FeedbackType type;

    @NotBlank(message = "Description must not be blank")
    private String description;

    private LocalDateTime sentAt;

    @NotNull(message = "User ID must not be null")
    private UUID userId;
}
