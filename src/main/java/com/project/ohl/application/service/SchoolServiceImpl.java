package com.project.ohl.application.service;

import com.project.ohl.application.mapper.SchoolMapper;
import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.application.port.in.SchoolService;
import com.project.ohl.application.port.out.SchoolRepositoryPort;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.School;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.SCHOOL_NOT_FOUND;

@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepositoryPort schoolRepository;

    private final SchoolMapper mapper;

    @Override
    public List<SchoolDto> getAllSchools() {
        List<School> schools = schoolRepository.findAll();
        return schools.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public SchoolDto getSchoolById(UUID id) {
        School school = getSchoolEntityById(id);
        return mapper.convertToDto(school);
    }

    @Override
    public SchoolDto saveSchool(SchoolDto schoolDto) {
        School school = mapper.convertToEntity(schoolDto);
        School savedSchool = schoolRepository.save(school);
        return mapper.convertToDto(savedSchool);
    }

    @Override
    public SchoolDto updateSchoolById(SchoolDto schoolDto, UUID id) {
        School schoolToUpdate = getSchoolEntityById(id);
        schoolToUpdate.setName(schoolDto.getName());
        schoolToUpdate.setCityId(schoolDto.getCityId());
        School updatedSchool = schoolRepository.update(schoolToUpdate);
        return mapper.convertToDto(updatedSchool);
    }

    @Override
    public void deleteSchoolById(UUID id) {
        School schoolToDelete = getSchoolEntityById(id);
        schoolRepository.delete(schoolToDelete);
    }

    @Override
    public School getSchoolEntityById(UUID id) {
        return schoolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(SCHOOL_NOT_FOUND, id)));
    }
}
