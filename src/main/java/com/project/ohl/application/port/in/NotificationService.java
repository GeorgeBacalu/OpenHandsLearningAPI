package com.project.ohl.application.port.in;

import com.project.ohl.application.model.NotificationDto;

import java.util.List;

public interface NotificationService {

    List<NotificationDto> getAllNotifications();

    NotificationDto getNotificationById(Long id);

    NotificationDto saveNotification(NotificationDto notificationDto);

    NotificationDto updateNotificationById(NotificationDto notificationDto, Long id);

    void deleteNotificationById(Long id);
}
