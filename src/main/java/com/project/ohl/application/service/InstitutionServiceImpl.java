package com.project.ohl.application.service;

import com.project.ohl.application.mapper.InstitutionMapper;
import com.project.ohl.application.model.InstitutionDto;
import com.project.ohl.application.port.in.InstitutionService;
import com.project.ohl.application.port.out.InstitutionRepositoryPort;
import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.domain.model.Institution;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.project.ohl.application.error.ExceptionMessageConstants.INSTITUTION_NOT_FOUND;

@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepositoryPort institutionRepository;

    private final InstitutionMapper mapper;

    @Override
    public List<InstitutionDto> getAllInstitutions() {
        List<Institution> institutions = institutionRepository.findAll();
        return institutions.stream().map(mapper::convertToDto).toList();
    }

    @Override
    public InstitutionDto getInstitutionById(UUID id) {
        Institution institution = getInstitutionEntityById(id);
        return mapper.convertToDto(institution);
    }

    @Override
    public InstitutionDto saveInstitution(InstitutionDto institutionDto) {
        Institution institution = mapper.convertToEntity(institutionDto);
        Institution savedInstitution = institutionRepository.save(institution);
        return mapper.convertToDto(savedInstitution);
    }

    @Override
    public InstitutionDto updateInstitutionById(InstitutionDto institutionDto, UUID id) {
        Institution institutionToUpdate = getInstitutionEntityById(id);
        institutionToUpdate.setSchool(institutionDto.getSchool());
        Institution updatedInstitution = institutionRepository.update(institutionToUpdate);
        return mapper.convertToDto(updatedInstitution);
    }

    @Override
    public void deleteInstitutionById(UUID id) {
        Institution institutionToDelete = getInstitutionEntityById(id);
        institutionRepository.delete(institutionToDelete);
    }

    @Override
    public Institution getInstitutionEntityById(UUID id) {
        return institutionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(INSTITUTION_NOT_FOUND, id)));
    }
}
