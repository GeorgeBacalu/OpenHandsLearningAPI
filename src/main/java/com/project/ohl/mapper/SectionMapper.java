package com.project.ohl.mapper;

import com.project.ohl.section.Section;
import com.project.ohl.section.SectionDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SectionMapper {

    public static SectionDto convertToDto(ModelMapper modelMapper, Section section) {
        return modelMapper.map(section, SectionDto.class);
    }

    public static Section convertToEntity(ModelMapper modelMapper, SectionDto sectionDto) {
        return modelMapper.map(sectionDto, Section.class);
    }
}
