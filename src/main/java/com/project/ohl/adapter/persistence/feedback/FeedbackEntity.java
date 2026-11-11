package com.project.ohl.adapter.persistence.feedback;

import com.project.ohl.adapter.persistence.PersistentEntity;
import com.project.ohl.adapter.persistence.user.UserEntity;
import com.project.ohl.domain.model.FeedbackType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feedbacks", indexes = {
      @Index(name = "idx_feedbacks_user", columnList = "user_id,sent_at")
})
public class FeedbackEntity extends PersistentEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private FeedbackType type;
    @Column(name = "description")
    private String description;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
