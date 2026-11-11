package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Notification;
import com.project.ohl.application.model.NotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class, uses = ReferenceMapper.class)
public interface NotificationMapper {
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "receiverId", source = "receiver.id")
    NotificationDto convertToDto(Notification model);

    @Mapping(target = "sender", source = "senderId")
    @Mapping(target = "receiver", source = "receiverId")
    Notification convertToEntity(NotificationDto dto);
}
