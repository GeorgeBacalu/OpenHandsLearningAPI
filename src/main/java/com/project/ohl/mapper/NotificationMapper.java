package com.project.ohl.mapper;

import com.project.ohl.notification.Notification;
import com.project.ohl.notification.NotificationDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationMapper {

    public static NotificationDto convertToDto(ModelMapper modelMapper, Notification notification) {
        return modelMapper.map(notification, NotificationDto.class);
    }

    public static Notification convertToEntity(ModelMapper modelMapper, NotificationDto notificationDto) {
        return modelMapper.map(notificationDto, Notification.class);
    }
}
