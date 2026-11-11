package com.project.ohl.application.model;

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
public class NotificationDto {

    private UUID id;

    @NotBlank(message = "Message must not be blank")
    @Size(max = 10000, message = "Message must not exceed 10000 characters")
    private String message;

    @NotNull(message = "User sender ID must not be null")
    private UUID senderId;

    @NotNull(message = "User receiver ID must not be null")
    private UUID receiverId;

    @Builder.Default
    private Boolean isRead = false;

    private Instant sentAt;
}
