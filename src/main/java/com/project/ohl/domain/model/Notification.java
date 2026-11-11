package com.project.ohl.domain.model;

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
public class Notification {

    private UUID id;
    private String message;
    private User sender;
    private User receiver;
    private Boolean isRead;
    private Instant sentAt;
}
