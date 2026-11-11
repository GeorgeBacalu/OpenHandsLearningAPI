package com.project.ohl.application.port.in;

import com.project.ohl.application.model.InstitutionDto;
import com.project.ohl.domain.model.Institution;
import java.util.List;
import java.util.UUID;

public interface InstitutionService {

    List<InstitutionDto> getAllInstitutions();

    InstitutionDto getInstitutionById(UUID id);

    InstitutionDto saveInstitution(InstitutionDto institutionDto);

    InstitutionDto updateInstitutionById(InstitutionDto institutionDto, UUID id);

    void deleteInstitutionById(UUID id);

    Institution getInstitutionEntityById(UUID id);
}
