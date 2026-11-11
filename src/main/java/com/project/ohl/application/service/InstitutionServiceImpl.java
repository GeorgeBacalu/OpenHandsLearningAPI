package com.project.ohl.application.service;

import com.project.ohl.application.model.InstitutionDto;
import com.project.ohl.application.port.in.InstitutionService;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.domain.model.Institution;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.INSTITUTION_NOT_FOUND;
import static com.project.ohl.application.mapper.InstitutionMapper.convertToDto;
import static com.project.ohl.application.mapper.InstitutionMapper.convertToEntity;

@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepositoryPort institutionRepository;

    @Override
    public List<InstitutionDto> getAllInstitutions() {
        List<Institution> institutions = institutionRepository.findAll();
        return institutions.stream().map(institution -> convertToDto(institution)).toList();
    }

    @Override
    public InstitutionDto getInstitutionById(Long id) {
        Institution institution = getInstitutionEntityById(id);
        return convertToDto(institution);
    }

    @Override
    public InstitutionDto saveInstitution(InstitutionDto institutionDto) {
        Institution institution = convertToEntity(institutionDto);
        Institution savedInstitution = institutionRepository.save(institution);
        return convertToDto(savedInstitution);
    }

    @Override
    public InstitutionDto updateInstitutionById(InstitutionDto institutionDto, Long id) {
        Institution institutionToUpdate = getInstitutionEntityById(id);
        institutionToUpdate.setSchool(institutionDto.getSchool());
        institutionToUpdate.setClassroom(institutionDto.getClassroom());
        Institution updatedInstitution = institutionRepository.update(institutionToUpdate);
        return convertToDto(updatedInstitution);
    }

    @Override
    public void deleteInstitutionById(Long id) {
        Institution institutionToDelete = getInstitutionEntityById(id);
        institutionRepository.delete(institutionToDelete);
    }

    @Override
    public Institution getInstitutionEntityById(Long id) {
        return institutionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(INSTITUTION_NOT_FOUND, id)));
    }
}
