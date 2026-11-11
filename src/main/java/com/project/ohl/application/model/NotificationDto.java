package com.project.ohl.application.model;

import com.project.ohl.domain.model.Notification;
import com.project.ohl.domain.model.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {

    @Positive(message = "Notification ID must be positive")
    private Long id;

    @NotBlank(message = "Message must not be blank")
    private String message;

    @Positive(message = "User sender ID must be positive")
    @NotNull(message = "User sender ID must not be null")
    private Long senderId;

    @Positive(message = "User receiver ID must be positive")
    @NotNull(message = "User receiver ID must not be null")
    private Long receiverId;

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime sentAt;
}
