package com.project.ohl.application.service;

import com.project.ohl.application.mapper.NotificationMapper;
import com.project.ohl.application.model.NotificationDto;
import com.project.ohl.application.port.in.NotificationService;
import com.project.ohl.application.port.in.UserService;
import com.project.ohl.application.port.out.NotificationRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Notification;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.NOTIFICATION_NOT_FOUND;

@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepositoryPort notificationRepository;
    private final UserService userService;
    private final Clock clock;

    private final NotificationMapper mapper;

    @Override
    public List<NotificationDto> getAllNotifications(UUID receiverId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new InvalidRequestException("Page must be non-negative and size between 1 and 100");
        List<Notification> notifications = notificationRepository.findPage(receiverId, page, size);
        return notifications.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public NotificationDto getNotificationById(UUID id) {
        Notification notification = getNotificationEntityById(id);
        return mapper.convertToDto(notification);
    }

    @Override
    public NotificationDto saveNotification(NotificationDto notificationDto) {
        Notification notification = mapper.convertToEntity(notificationDto);
        notification.setSentAt(Instant.now(clock));
        Notification savedNotification = notificationRepository.save(notification);
        return mapper.convertToDto(savedNotification);
    }

    @Override
    public NotificationDto updateNotificationById(NotificationDto notificationDto, UUID id) {
        Notification notificationToUpdate = getNotificationEntityById(id);
        notificationToUpdate.setMessage(notificationDto.getMessage());
        notificationToUpdate.setIsRead(notificationDto.getIsRead());
        notificationToUpdate.setSender(userService.getUserEntityById(notificationDto.getSenderId()));
        notificationToUpdate.setReceiver(userService.getUserEntityById(notificationDto.getReceiverId()));
        Notification updatedNotification = notificationRepository.update(notificationToUpdate);
        return mapper.convertToDto(updatedNotification);
    }

    @Override
    public void deleteNotificationById(UUID id) {
        Notification notificationToDelete = getNotificationEntityById(id);
        notificationRepository.delete(notificationToDelete);
    }

    private Notification getNotificationEntityById(UUID id) {
        return notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(NOTIFICATION_NOT_FOUND, id)));
    }
}
