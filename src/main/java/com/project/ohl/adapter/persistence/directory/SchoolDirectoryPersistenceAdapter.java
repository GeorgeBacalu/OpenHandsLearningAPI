package com.project.ohl.adapter.persistence.directory;

import com.project.ohl.adapter.persistence.school.SchoolJpaRepository;
import com.project.ohl.adapter.persistence.user.UserJpaRepository;
import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.domain.model.SchoolDirectory;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.project.ohl.domain.model.SchoolDirectory.sourceId;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolDirectoryPersistenceAdapter implements SchoolDirectoryPort {
    private final CountyJpaRepository counties;
    private final CityJpaRepository cities;
    private final SchoolJpaRepository schools;
    private final UserJpaRepository users;
    private final DirectoryJpaImporter importer;

    @Override
    public List<SchoolDirectory.County> counties() {
        return counties.findAllByOrderByNameAscIdAsc().stream()
                .map(row -> new SchoolDirectory.County(row.getId(), row.getName())).toList();
    }

    @Override
    public List<SchoolDirectory.City> cities(UUID countyId, String name, int offset, int limit) {
        return cities.search(countyId, pattern(name), offset, limit).stream()
                .map(row -> new SchoolDirectory.City(row.getId(), row.getName(), row.getCounty().getId())).toList();
    }

    @Override
    public List<SchoolDirectory.School> schools(UUID cityId, String name, int offset, int limit) {
        return schools.search(cityId, pattern(name), offset, limit).stream()
                .map(row -> new SchoolDirectory.School(row.getId(), row.getName(), row.getCity().getId())).toList();
    }

    private static String pattern(String value) {
        return "%" + value.strip().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    @Override
    @Transactional
    public void importDirectory(SchoolDirectory data) {
        counties.lockImport();
        importer.importDirectory(data);
        replacePlaceholders();
        counties.validateSchoolCities();
    }

    private void replacePlaceholders() {
        var replacements = Map.of(
                UUID.fromString("103ac659-e577-388b-a794-67fff7f04ca3"), new String[]{"Scoala Gimnaziala Nr. 1", "2361101277"},
                UUID.fromString("d507dc18-15f4-35b8-83ec-548ee8b2ffc6"), new String[]{"Scoala Gimnaziala Nr. 2", "2362101883"},
                UUID.fromString("e5bfa376-f632-3683-8207-91fc4924cc50"), new String[]{"Scoala Gimnaziala Nr. 3", "2362100534"});
        replacements.forEach((oldId, school) -> {
            var existing = schools.findByIdForUpdate(oldId);
            if (existing.isEmpty()) return;
            if (!school[0].equals(existing.orElseThrow().getName())) throw new IllegalStateException("Placeholder school has been changed; review its replacement");
            UUID replacement = sourceId("school", school[1]);
            if (!schools.existsById(replacement))
                throw new IllegalStateException("Replacement school is absent from the government dataset");
            users.reassignSchool(oldId, schools.getReferenceById(replacement));
            schools.deleteById(oldId);
        });
    }
}
