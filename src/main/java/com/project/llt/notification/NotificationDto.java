package com.project.llt.notification;

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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sentAt;
}
