package com.project.ohl.adapter.persistence.notification;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
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
@Table(name = "notifications", indexes = {
      @Index(name = "idx_notifications_receiver", columnList = "receiver_id,sent_at"),
      @Index(name = "idx_notifications_sender", columnList = "sender_id")
})
public class NotificationEntity extends PersistentEntity {
    @Column(name = "message")
    private String message;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserEntity sender;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver;
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
