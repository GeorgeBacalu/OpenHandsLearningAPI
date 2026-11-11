package com.project.ohl.application.mapper;

import com.project.ohl.domain.model.Institution;
import com.project.ohl.application.model.InstitutionDto;

public final class InstitutionMapper {
    private InstitutionMapper() {
    }

    public static InstitutionDto convertToDto(Institution model) {
        return InstitutionDto.builder().id(model.getId())
              .school(model.getSchool())
              .classroom(model.getClassroom())
              .build();
    }

    public static Institution convertToEntity(InstitutionDto dto) {
        return Institution.builder().id(dto.getId())
              .school(dto.getSchool())
              .classroom(dto.getClassroom())
              .build();
    }
}
