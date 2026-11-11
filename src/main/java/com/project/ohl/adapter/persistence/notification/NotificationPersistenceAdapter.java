package com.project.ohl.adapter.persistence.notification;

import com.project.ohl.adapter.persistence.JpaPersistenceAdapter;
import com.project.ohl.adapter.persistence.user.UserEntity;
import com.project.ohl.domain.model.Notification;
import com.project.ohl.application.port.out.NotificationRepositoryPort;
import com.project.ohl.domain.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationPersistenceAdapter extends JpaPersistenceAdapter<Notification, NotificationEntity> implements NotificationRepositoryPort {

    public NotificationPersistenceAdapter(NotificationJpaRepository repository) {
        super(repository);
    }

    @Override
    protected NotificationEntity newEntity() {
        return new NotificationEntity();
    }

    @Override
    protected Long idOf(Notification model) {
        return model.getId();
    }

    @Override
    protected Notification toDomain(NotificationEntity entity) {
        return Notification.builder().id(entity.getId())
              .message(entity.getMessage())
              .sender(entity.getSender() == null ? null : User.builder().id(entity.getSender().getId()).build())
              .receiver(entity.getReceiver() == null ? null : User.builder().id(entity.getReceiver().getId()).build())
              .isRead(Boolean.TRUE.equals(entity.getIsRead()))
              .sentAt(entity.getSentAt())
              .build();
    }

    @Override
    protected void apply(Notification model, NotificationEntity entity) {
        entity.setMessage(model.getMessage());
        entity.setSender(reference(UserEntity.class, model.getSender() == null ? null : model.getSender().getId()));
        entity.setReceiver(reference(UserEntity.class, model.getReceiver() == null ? null : model.getReceiver().getId()));
        entity.setIsRead(Boolean.TRUE.equals(model.getIsRead()));
        entity.setSentAt(model.getSentAt());
    }
}
