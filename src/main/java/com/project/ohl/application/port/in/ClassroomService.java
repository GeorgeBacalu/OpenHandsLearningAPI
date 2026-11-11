package com.project.ohl.application.port.in;

import com.project.ohl.application.model.ClassroomDto;
import java.util.List;
import java.util.UUID;

public interface ClassroomService {
    List<ClassroomDto> getAllClassrooms();
    ClassroomDto getClassroomById(UUID id);
    ClassroomDto saveClassroom(ClassroomDto model);
    ClassroomDto updateClassroomById(ClassroomDto model, UUID id);
    void deleteClassroomById(UUID id);
}
