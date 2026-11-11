package com.project.ohl.application.model;

import com.project.ohl.domain.model.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
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
    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    private String description;

    private Instant sentAt;

    @NotNull(message = "User ID must not be null")
    private UUID userId;
}
