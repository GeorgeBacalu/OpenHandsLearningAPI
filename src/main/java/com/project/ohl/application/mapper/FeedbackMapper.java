package com.project.ohl.application.mapper;

import com.project.ohl.application.model.FeedbackDto;
import com.project.ohl.domain.model.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class, uses = ReferenceMapper.class)
public interface FeedbackMapper {
    @Mapping(target = "userId", source = "user.id")
    FeedbackDto convertToDto(Feedback model);

    @Mapping(target = "user", source = "userId")
    Feedback convertToEntity(FeedbackDto dto);
}
