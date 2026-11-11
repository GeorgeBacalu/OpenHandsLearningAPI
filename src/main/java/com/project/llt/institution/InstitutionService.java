package com.project.llt.institution;

import java.util.List;

public interface InstitutionService {

    List<InstitutionDto> getAllInstitutions();

    InstitutionDto getInstitutionById(Long id);

    InstitutionDto saveInstitution(InstitutionDto institutionDto);

    InstitutionDto updateInstitutionById(InstitutionDto institutionDto, Long id);

    void deleteInstitutionById(Long id);

    Institution getInstitutionEntityById(Long id);
}
