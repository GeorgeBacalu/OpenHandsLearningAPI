package com.project.llt.mapper;

import com.project.llt.institution.Institution;
import com.project.llt.institution.InstitutionDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstitutionMapper {

    public static InstitutionDto convertToDto(ModelMapper modelMapper, Institution institution) {
        return modelMapper.map(institution, InstitutionDto.class);
    }

    public static Institution convertToEntity(ModelMapper modelMapper, InstitutionDto institutionDto) {
        return modelMapper.map(institutionDto, Institution.class);
    }
}
