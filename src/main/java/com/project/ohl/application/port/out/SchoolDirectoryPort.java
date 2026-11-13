package com.project.ohl.application.port.out;

import com.project.ohl.domain.model.SchoolDirectory;
import java.util.List;
import java.util.UUID;

public interface SchoolDirectoryPort {
    List<SchoolDirectory.County> counties();
    List<SchoolDirectory.City> cities(UUID countyId, String name, int offset, int limit);
    List<SchoolDirectory.School> schools(UUID cityId, String name, int offset, int limit);
    void importDirectory(SchoolDirectory directory);
}
