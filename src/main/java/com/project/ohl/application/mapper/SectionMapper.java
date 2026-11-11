package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Section;
import com.project.ohl.application.model.SectionDto;

public final class SectionMapper {
    private SectionMapper() {
    }

    public static SectionDto convertToDto(Section model) {
        return SectionDto.builder().id(model.getId())
              .name(model.getName())
              .iconId(model.getIconId())
              .imageId(model.getImageId())
              .build();
    }

    public static Section convertToEntity(SectionDto dto) {
        return Section.builder().id(dto.getId())
              .name(dto.getName())
              .iconId(dto.getIconId())
              .imageId(dto.getImageId())
              .build();
    }
}
