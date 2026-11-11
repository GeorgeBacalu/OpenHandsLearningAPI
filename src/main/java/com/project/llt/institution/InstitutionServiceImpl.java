package com.project.llt.institution;

import com.project.llt.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.project.llt.constants.ExceptionMessageConstants.INSTITUTION_NOT_FOUND;
import static com.project.llt.mapper.InstitutionMapper.convertToDto;
import static com.project.llt.mapper.InstitutionMapper.convertToEntity;

@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionDao institutionDao;
    private final ModelMapper modelMapper;

    @Override
    public List<InstitutionDto> getAllInstitutions() {
        List<Institution> institutions = institutionDao.findAll();
        return !institutions.isEmpty() ? institutions.stream().map(institution -> convertToDto(modelMapper, institution)).toList() : new ArrayList<>();
    }

    @Override
    public InstitutionDto getInstitutionById(Long id) {
        Institution institution = getInstitutionEntityById(id);
        return convertToDto(modelMapper, institution);
    }

    @Override
    public InstitutionDto saveInstitution(InstitutionDto institutionDto) {
        Institution institution = convertToEntity(modelMapper, institutionDto);
        Institution savedInstitution = institutionDao.save(institution);
        return convertToDto(modelMapper, savedInstitution);
    }

    @Override
    public InstitutionDto updateInstitutionById(InstitutionDto institutionDto, Long id) {
        Institution institutionToUpdate = getInstitutionEntityById(id);
        institutionToUpdate.setSchool(institutionDto.getSchool());
        institutionToUpdate.setClassroom(institutionDto.getClassroom());
        Institution updatedInstitution = institutionDao.update(institutionToUpdate);
        return convertToDto(modelMapper, updatedInstitution);
    }

    @Override
    public void deleteInstitutionById(Long id) {
        Institution institutionToDelete = getInstitutionEntityById(id);
        institutionDao.delete(institutionToDelete);
    }

    @Override
    public Institution getInstitutionEntityById(Long id) {
        return institutionDao.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(INSTITUTION_NOT_FOUND, id)));
    }
}
