package com.project.ohl.application.port.in;

import com.project.ohl.application.model.NotificationDto;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationDto> getAllNotifications();

    NotificationDto getNotificationById(UUID id);

    NotificationDto saveNotification(NotificationDto notificationDto);

    NotificationDto updateNotificationById(NotificationDto notificationDto, UUID id);

    void deleteNotificationById(UUID id);
}
