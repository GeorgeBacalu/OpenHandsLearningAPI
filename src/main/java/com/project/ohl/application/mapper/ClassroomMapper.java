package com.project.ohl.application.mapper;

import com.project.ohl.application.model.ClassroomDto;
import com.project.ohl.domain.model.Classroom;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfiguration.class)
public interface ClassroomMapper {
    ClassroomDto convertToDto(Classroom model);
    Classroom convertToEntity(ClassroomDto dto);
    List<ClassroomDto> convertToDtoList(List<Classroom> models);
}
