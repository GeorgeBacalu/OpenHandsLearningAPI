package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Feedback;
import com.project.ohl.application.model.FeedbackDto;
import com.project.ohl.domain.model.User;

public final class FeedbackMapper {
    private FeedbackMapper() {
    }

    public static FeedbackDto convertToDto(Feedback model) {
        return FeedbackDto.builder().id(model.getId())
              .type(model.getType())
              .description(model.getDescription())
              .sentAt(model.getSentAt())
              .userId(model.getUser() == null ? null : model.getUser().getId())
              .build();
    }

    public static Feedback convertToEntity(FeedbackDto dto) {
        return Feedback.builder().id(dto.getId())
              .type(dto.getType())
              .description(dto.getDescription())
              .sentAt(dto.getSentAt())
              .user(dto.getUserId() == null ? null : User.builder().id(dto.getUserId()).build())
              .build();
    }
}
