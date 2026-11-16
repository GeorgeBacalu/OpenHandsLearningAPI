package com.project.ohl.adapter.transaction;

import com.project.ohl.application.port.in.SchoolDirectoryService;
import com.project.ohl.domain.model.SchoolDirectory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionalSchoolDirectoryService implements SchoolDirectoryService {
    private final SchoolDirectoryService delegate;

    @Override
    @Transactional
    public void importDirectory(SchoolDirectory directory) {
        delegate.importDirectory(directory);
    }

    @Override
    public List<SchoolDirectory.County> counties() {
        return delegate.counties();
    }

    @Override
    public List<SchoolDirectory.City> cities(UUID countyId, String name, int offset, int limit) {
        return delegate.cities(countyId, name, offset, limit);
    }

    @Override
    public List<SchoolDirectory.School> schools(UUID cityId, String name, int offset, int limit) {
        return delegate.schools(cityId, name, offset, limit);
    }
}
