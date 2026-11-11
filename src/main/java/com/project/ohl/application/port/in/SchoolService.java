package com.project.ohl.application.port.in;

import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.domain.model.School;
import java.util.List;
import java.util.UUID;

public interface SchoolService {

    List<SchoolDto> getAllSchools();

    SchoolDto getSchoolById(UUID id);

    SchoolDto saveSchool(SchoolDto schoolDto);

    SchoolDto updateSchoolById(SchoolDto schoolDto, UUID id);

    void deleteSchoolById(UUID id);

    School getSchoolEntityById(UUID id);
}
