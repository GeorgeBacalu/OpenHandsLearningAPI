package com.project.ohl.application.mapper;

import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.domain.model.School;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MappingConfiguration.class)
public interface SchoolMapper {
    SchoolDto convertToDto(School model);

    @Mapping(target = "cityName", ignore = true)
    @Mapping(target = "countyName", ignore = true)
    School convertToEntity(SchoolDto dto);
}
