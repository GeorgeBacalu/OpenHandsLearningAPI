package com.project.ohl.adapter.transaction;

import com.project.ohl.application.model.SchoolDto;
import com.project.ohl.application.port.in.SchoolService;
import com.project.ohl.domain.model.School;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionalSchoolService implements SchoolService {
    private final SchoolService delegate;

    @Override
    public List<SchoolDto> getAllSchools() {
        return delegate.getAllSchools();
    }

    @Override
    public SchoolDto getSchoolById(UUID id) {
        return delegate.getSchoolById(id);
    }

    @Override
    @Transactional
    public SchoolDto saveSchool(SchoolDto schoolDto) {
        return delegate.saveSchool(schoolDto);
    }

    @Override
    @Transactional
    public SchoolDto updateSchoolById(SchoolDto schoolDto, UUID id) {
        return delegate.updateSchoolById(schoolDto, id);
    }

    @Override
    @Transactional
    public void deleteSchoolById(UUID id) {
        delegate.deleteSchoolById(id);
    }

    @Override
    public School getSchoolEntityById(UUID id) {
        return delegate.getSchoolEntityById(id);
    }
}
