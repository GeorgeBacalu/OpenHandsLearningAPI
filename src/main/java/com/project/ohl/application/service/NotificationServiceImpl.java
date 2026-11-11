package com.project.ohl.application.service;

import com.project.ohl.application.mapper.NotificationMapper;
import com.project.ohl.application.model.NotificationDto;
import com.project.ohl.application.port.in.NotificationService;
import com.project.ohl.application.port.out.NotificationRepositoryPort;
import com.project.ohl.domain.model.Notification;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.application.port.in.UserService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.NOTIFICATION_NOT_FOUND;

@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepositoryPort notificationRepository;
    private final UserService userService;
    private final Clock clock;

    private final NotificationMapper mapper;

    @Override
    public List<NotificationDto> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public NotificationDto getNotificationById(Long id) {
        Notification notification = getNotificationEntityById(id);
        return mapper.convertToDto(notification);
    }

    @Override
    public NotificationDto saveNotification(NotificationDto notificationDto) {
        Notification notification = mapper.convertToEntity(notificationDto);
        notification.setSentAt(LocalDateTime.now(clock));
        Notification savedNotification = notificationRepository.save(notification);
        return mapper.convertToDto(savedNotification);
    }

    @Override
    public NotificationDto updateNotificationById(NotificationDto notificationDto, Long id) {
        Notification notificationToUpdate = getNotificationEntityById(id);
        notificationToUpdate.setMessage(notificationDto.getMessage());
        notificationToUpdate.setIsRead(notificationDto.getIsRead());
        notificationToUpdate.setSender(userService.getUserEntityById(notificationDto.getSenderId()));
        notificationToUpdate.setReceiver(userService.getUserEntityById(notificationDto.getReceiverId()));
        Notification updatedNotification = notificationRepository.update(notificationToUpdate);
        return mapper.convertToDto(updatedNotification);
    }

    @Override
    public void deleteNotificationById(Long id) {
        Notification notificationToDelete = getNotificationEntityById(id);
        notificationRepository.delete(notificationToDelete);
    }

    private Notification getNotificationEntityById(Long id) {
        return notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(NOTIFICATION_NOT_FOUND, id)));
    }
}
