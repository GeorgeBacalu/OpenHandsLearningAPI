package com.project.llt.notification;

import java.util.List;

public interface NotificationService {

    List<NotificationDto> getAllNotifications();

    NotificationDto getNotificationById(Long id);

    NotificationDto saveNotification(NotificationDto notificationDto);

    NotificationDto updateNotificationById(NotificationDto notificationDto, Long id);

    void deleteNotificationById(Long id);
}
