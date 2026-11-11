package com.project.ohl.adapter.persistence.notification;

import com.project.ohl.adapter.persistence.PersistentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class NotificationEntity extends PersistentEntity {
    @Column(name = "message")
    private String message;
    @Column(name = "sender_id")
    private Long senderId;
    @Column(name = "receiver_id")
    private Long receiverId;
    @Column(name = "is_read")
    private Boolean isRead;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
