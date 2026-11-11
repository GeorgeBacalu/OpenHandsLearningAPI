package com.project.ohl.application.service;

import com.project.ohl.application.mapper.ClassroomMapper;
import com.project.ohl.application.model.ClassroomDto;
import com.project.ohl.application.port.in.ClassroomService;
import com.project.ohl.application.port.out.ClassroomRepositoryPort;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Classroom;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {
    private final ClassroomRepositoryPort classrooms;
    private final ClassroomMapper mapper;
    public List<ClassroomDto> getAllClassrooms() { return mapper.convertToDtoList(classrooms.findAll()); }
    public ClassroomDto getClassroomById(UUID id) { return mapper.convertToDto(find(id)); }
    public ClassroomDto saveClassroom(ClassroomDto model) { return mapper.convertToDto(classrooms.save(mapper.convertToEntity(model))); }
    public ClassroomDto updateClassroomById(ClassroomDto model, UUID id) {
        Classroom current = find(id);
        current.setName(model.getName()); current.setInstitutionId(model.getInstitutionId());
        return mapper.convertToDto(classrooms.update(current));
    }
    public void deleteClassroomById(UUID id) { classrooms.delete(find(id)); }
    private Classroom find(UUID id) { return classrooms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Classroom " + id + " was not found")); }
}
