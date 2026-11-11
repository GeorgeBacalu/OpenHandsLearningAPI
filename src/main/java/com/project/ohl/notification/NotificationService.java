package com.project.ohl.notification;

import java.util.List;

public interface NotificationService {

    List<NotificationDto> getAllNotifications();

    NotificationDto getNotificationById(Long id);

    NotificationDto saveNotification(NotificationDto notificationDto);

    NotificationDto updateNotificationById(NotificationDto notificationDto, Long id);

    void deleteNotificationById(Long id);
}
