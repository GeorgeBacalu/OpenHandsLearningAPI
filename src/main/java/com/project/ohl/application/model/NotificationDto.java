package com.project.ohl.application.model;

import java.util.UUID;
import com.project.ohl.domain.model.Notification;
import com.project.ohl.domain.model.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private UUID id;

    @NotBlank(message = "Message must not be blank")
    private String message;

    @NotNull(message = "User sender ID must not be null")
    private UUID senderId;

    @NotNull(message = "User receiver ID must not be null")
    private UUID receiverId;

    @Builder.Default
    private Boolean isRead = false;

    private LocalDateTime sentAt;
}
