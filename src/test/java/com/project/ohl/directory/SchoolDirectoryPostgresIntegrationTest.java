package com.project.ohl.directory;

import com.project.ohl.DatabaseIntegrationTest;
import com.project.ohl.TestIds;
import com.project.ohl.adapter.persistence.school.SchoolEntity;
import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.domain.model.SchoolDirectory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import static com.project.ohl.domain.model.SchoolDirectory.sourceId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Set OHL_POSTGRES_TEST_URL to a disposable PostgreSQL database; Hibernate recreates its tables. */
@EnabledIfEnvironmentVariable(named = "OHL_POSTGRES_TEST_URL", matches = ".+")
@SpringBootTest(properties = {
        "spring.datasource.url=${OHL_POSTGRES_TEST_URL}",
        "spring.datasource.username=${POSTGRES_USERNAME}",
        "spring.datasource.password=${POSTGRES_PASSWORD}"})
class SchoolDirectoryPostgresIntegrationTest extends DatabaseIntegrationTest {
    private static final UUID COUNTY = sourceId("county", "test");
    private static final UUID CITY = sourceId("city", "test");
    private static final UUID LEGACY_SCHOOL = UUID.fromString("103ac659-e577-388b-a794-67fff7f04ca3");
    private static final UUID REPLACEMENT = sourceId("school", "2361101277");
    @Autowired private SchoolDirectoryPort directory;

    @BeforeEach
    void addLegacyCityConstraint() {
        executeNative("ALTER TABLE schools DROP CONSTRAINT IF EXISTS schools_city_required");
        executeNative("ALTER TABLE schools ADD CONSTRAINT schools_city_required CHECK (city_id IS NOT NULL) NOT VALID");
    }

    @Test void importsAcrossBatchesWithoutChangingSourceIdsOrRewritingUnchangedRows() {
        var data = data(501);
        inTransaction(() -> {
            directory.importDirectory(data);
            assertThat(schemaCount("SELECT count(*) FROM pg_locks WHERE pid = pg_backend_pid() "
                    + "AND locktype = 'advisory' AND classid = 0 AND objid = 782134920 AND granted")).isOne();
        });
        assertThat(schemaCount("SELECT count(*) FROM pg_locks WHERE pid = pg_backend_pid() "
                + "AND locktype = 'advisory' AND classid = 0 AND objid = 782134920 AND granted")).isZero();
        assertThat(schoolRepository.count()).isEqualTo(503);
        assertThat(schoolRepository.existsById(data.schools().getLast().id())).isTrue();
        var versions = nativeValues("SELECT xmin::text FROM schools ORDER BY id", String.class);

        directory.importDirectory(data);

        assertThat(schoolRepository.count()).isEqualTo(503);
        assertThat(nativeValues("SELECT xmin::text FROM schools ORDER BY id", String.class)).isEqualTo(versions);
        assertThat(schemaCount("SELECT count(*) FROM pg_constraint WHERE conname = 'schools_city_required' AND convalidated")).isOne();

        // Use an existing fixture city to verify that both the name and parent are updated.
        UUID fixtureCity = UUID.fromString("00000000-0000-4000-8000-000000000104");
        var changed = new SchoolDirectory(data.counties(), data.cities(),
                List.of(new SchoolDirectory.School(data.schools().getFirst().id(), "Renamed", fixtureCity)));
        directory.importDirectory(changed);
        assertThat(directory.schools(fixtureCity, "renamed", 0, 50)).singleElement()
                .satisfies(row -> assertThat(row.id()).isEqualTo(data.schools().getFirst().id()));

        UUID fixtureCounty = UUID.fromString("00000000-0000-4000-8000-000000000102");
        directory.importDirectory(new SchoolDirectory(List.of(new SchoolDirectory.County(COUNTY, "Renamed county")),
                List.of(new SchoolDirectory.City(CITY, "Renamed city", fixtureCounty)), List.of()));
        assertThat(countyRepository.findById(COUNTY).orElseThrow().getName()).isEqualTo("Renamed county");
        assertThat(directory.cities(fixtureCounty, "renamed", 0, 50)).singleElement()
                .satisfies(row -> assertThat(row.id()).isEqualTo(CITY));
    }

    @Test void rollsBackEarlierBatchesWhenALaterBatchViolatesAForeignKey() {
        var valid = data(501);
        var schools = new ArrayList<>(valid.schools());
        schools.add(new SchoolDirectory.School(UUID.randomUUID(), "Invalid", UUID.randomUUID()));

        assertThatThrownBy(() -> directory.importDirectory(new SchoolDirectory(valid.counties(), valid.cities(), schools)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(countyRepository.existsById(COUNTY)).isFalse();
        assertThat(cityRepository.existsById(CITY)).isFalse();
        assertThat(schoolRepository.count()).isEqualTo(2);
    }

    @Test void replacesLegacySchoolsAndReassignsTheirUsers() {
        legacySchool("Scoala Gimnaziala Nr. 1");
        var data = data(1);
        var replacement = new SchoolDirectory(data.counties(), data.cities(),
                List.of(new SchoolDirectory.School(REPLACEMENT, "Government school", CITY)));

        directory.importDirectory(replacement);

        assertThat(schoolRepository.existsById(LEGACY_SCHOOL)).isFalse();
        inTransaction(() -> assertThat(userRepository.findById(TestIds.USER_ANA).orElseThrow().getSchool().getId())
                .isEqualTo(REPLACEMENT));
    }

    @Test void refusesChangedLegacySchoolsAndRollsBackTheImport() {
        legacySchool("Locally changed school");

        assertThatThrownBy(() -> directory.importDirectory(data(1)))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class).hasMessageContaining("has been changed");
        assertThat(countyRepository.existsById(COUNTY)).isFalse();
        assertThat(schoolRepository.existsById(LEGACY_SCHOOL)).isTrue();
    }

    @Test void refusesMissingReplacementSchoolsAndRollsBackTheImport() {
        legacySchool("Scoala Gimnaziala Nr. 1");

        assertThatThrownBy(() -> directory.importDirectory(data(1)))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class).hasMessageContaining("absent");
        assertThat(countyRepository.existsById(COUNTY)).isFalse();
        assertThat(schoolRepository.existsById(LEGACY_SCHOOL)).isTrue();
    }

    private void legacySchool(String name) {
        var school = new SchoolEntity();
        school.setId(LEGACY_SCHOOL);
        school.setName(name);
        school.setCity(cityRepository.getReferenceById(UUID.fromString("00000000-0000-4000-8000-000000000104")));
        schoolRepository.saveAndFlush(school);
        updateUser(TestIds.USER_ANA, user -> user.setSchool(schoolRepository.getReferenceById(LEGACY_SCHOOL)));
    }

    private static SchoolDirectory data(int size) {
        return new SchoolDirectory(List.of(new SchoolDirectory.County(COUNTY, "Imported county")),
                List.of(new SchoolDirectory.City(CITY, "Imported city", COUNTY)),
                IntStream.range(0, size).mapToObj(index -> new SchoolDirectory.School(sourceId("school", "test-" + index),
                        "Imported school " + index, CITY)).toList());
    }
}
