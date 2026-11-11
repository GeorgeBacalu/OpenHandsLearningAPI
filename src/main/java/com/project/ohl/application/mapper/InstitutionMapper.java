package com.project.ohl.application.mapper;

import com.project.ohl.application.model.InstitutionDto;
import com.project.ohl.domain.model.Institution;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfiguration.class)
public interface InstitutionMapper {
    InstitutionDto convertToDto(Institution model);

    Institution convertToEntity(InstitutionDto dto);
}
