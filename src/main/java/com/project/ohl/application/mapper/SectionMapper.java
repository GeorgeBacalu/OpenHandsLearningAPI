package com.project.ohl.application.mapper;

import com.project.ohl.application.model.SectionDto;
import com.project.ohl.domain.model.Section;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfiguration.class)
public interface SectionMapper {
    SectionDto convertToDto(Section model);

    Section convertToEntity(SectionDto dto);
}
