package com.project.ohl.application.mapper;

import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.domain.model.School;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfiguration.class)
public interface SchoolMapper {
    SchoolDto convertToDto(School model);

    School convertToEntity(SchoolDto dto);
}
