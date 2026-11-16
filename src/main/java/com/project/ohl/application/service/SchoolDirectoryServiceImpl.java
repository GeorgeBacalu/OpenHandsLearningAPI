package com.project.ohl.application.service;

import com.project.ohl.application.port.in.SchoolDirectoryService;
import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.SchoolDirectory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchoolDirectoryServiceImpl implements SchoolDirectoryService {
    private final SchoolDirectoryPort directory;

    @Override public void importDirectory(SchoolDirectory data) {
        if (data.counties().size() != 42 || data.cities().size() < 13000 || data.schools().size() < 15000)
            throw new IllegalArgumentException("A complete Romania directory is required");
        directory.importDirectory(data);
    }

    @Override public List<SchoolDirectory.County> counties() {
        return directory.counties();
    }

    @Override public List<SchoolDirectory.City> cities(UUID countyId, String name, int offset, int limit) {
        validatePage(offset, limit);
        return directory.cities(countyId, name, offset, limit);
    }

    @Override public List<SchoolDirectory.School> schools(UUID cityId, String name, int offset, int limit) {
        validatePage(offset, limit);
        return directory.schools(cityId, name, offset, limit);
    }

    private static void validatePage(int offset, int limit) {
        if (offset < 0 || limit < 1 || limit > 200) {
            throw new InvalidRequestException("Use offset >= 0 and limit between 1 and 200.");
        }
    }
}
