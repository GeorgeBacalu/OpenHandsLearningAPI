package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Notification;
import com.project.ohl.application.model.NotificationDto;
import com.project.ohl.domain.model.User;

public final class NotificationMapper {
    private NotificationMapper() {
    }

    public static NotificationDto convertToDto(Notification model) {
        return NotificationDto.builder().id(model.getId())
              .message(model.getMessage())
              .isRead(model.getIsRead())
              .sentAt(model.getSentAt())
              .senderId(model.getSender() == null ? null : model.getSender().getId())
              .receiverId(model.getReceiver() == null ? null : model.getReceiver().getId())
              .build();
    }

    public static Notification convertToEntity(NotificationDto dto) {
        return Notification.builder().id(dto.getId())
              .message(dto.getMessage())
              .isRead(dto.getIsRead())
              .sentAt(dto.getSentAt())
              .sender(dto.getSenderId() == null ? null : User.builder().id(dto.getSenderId()).build())
              .receiver(dto.getReceiverId() == null ? null : User.builder().id(dto.getReceiverId()).build())
              .build();
    }
}
