package com.project.llt.notification;

import java.util.List;
import java.util.Optional;

public interface NotificationDao {

    List<Notification> findAll();

    Optional<Notification> findById(Long id);

    Notification save(Notification notification);

    Notification update(Notification notification);

    void delete(Notification notification);
}
