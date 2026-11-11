package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    List<Notification> findAll();

    Optional<Notification> findById(UUID id);

    Notification save(Notification notification);

    Notification update(Notification notification);

    void delete(Notification notification);
}
