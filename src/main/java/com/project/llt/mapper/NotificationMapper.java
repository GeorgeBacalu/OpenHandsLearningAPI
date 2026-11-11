package com.project.llt.mapper;

import com.project.llt.notification.Notification;
import com.project.llt.notification.NotificationDto;
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
