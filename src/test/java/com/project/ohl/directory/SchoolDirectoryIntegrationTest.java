package com.project.ohl.directory;

import com.project.ohl.DatabaseIntegrationTest;
import com.project.ohl.adapter.persistence.directory.CityEntity;
import com.project.ohl.adapter.persistence.school.SchoolEntity;
import com.project.ohl.adapter.web.SchoolDirectoryController;
import com.project.ohl.application.port.in.SchoolDirectoryService;
import com.project.ohl.domain.exception.InvalidRequestException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SchoolDirectoryIntegrationTest extends DatabaseIntegrationTest {
    @Autowired SchoolDirectoryService directory;
    @Autowired SchoolDirectoryController controller;

    @Test void filtersEachLevelByItsParentAndSupportsSchoolSearch() {
        var county = controller.counties().stream().filter(value -> value.name().equals("Cluj")).findFirst().orElseThrow();
        var city = controller.cities(county.id(), "cluj", 0, 1).getFirst();

        assertThat(controller.schools(city.id(), "school", 0, 1)).singleElement()
                .satisfies(school -> assertThat(school.name()).isEqualTo("School B"));
        assertThat(directory.cities(county.id(), "%", 0, 50)).isEmpty();
        assertThat(directory.cities(county.id(), "", 1, 50)).isEmpty();
        assertThat(directory.schools(UUID.randomUUID(), "", 0, 50)).isEmpty();

        assertThatThrownBy(() -> directory.cities(county.id(), "", -1, 50)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> directory.schools(city.id(), "", 0, 201)).isInstanceOf(InvalidRequestException.class);
    }

    @Test void schoolsCanShareNamesAndCannotReferenceMissingCities() {
        UUID city = UUID.fromString("00000000-0000-4000-8000-000000000104");

        createSchool("School B", city);

        assertThat(directory.schools(city, "School B", 0, 50)).hasSize(1);

        assertThatThrownBy(() -> createSchool("Invalid school", UUID.randomUUID())).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> createSchool("Missing city", null)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void schoolSearchPreservesLiteralWildcardsAndArbitraryOffsets() {
        UUID city = UUID.fromString("00000000-0000-4000-8000-000000000104");
        for (String name : List.of("School 0", "School 1", "School 2", "100% school", "Under_score", "Bang!", "Back\\slash")) {
            createSchool(name, city);
        }

        assertThat(directory.schools(city, " SCHOOL ", 1, 2)).extracting(value -> value.name())
                .containsExactly("School 0", "School 1");
        for (String literal : List.of("%", "_", "!", "\\")) {
            assertThat(directory.schools(city, literal, 0, 50)).hasSize(1);
        }
        assertThat(directory.schools(city, "school", 100, 2)).isEmpty();
    }

    @Test void citySearchPreservesLiteralWildcardsAndArbitraryOffsets() {
        UUID county = UUID.fromString("00000000-0000-4000-8000-000000000102");
        for (String name : List.of("City 0", "City 1", "City 2", "100% City", "Under_score", "Bang!", "Back\\slash")) {
            var city = new CityEntity();
            city.setName(name);
            city.setCounty(countyRepository.getReferenceById(county));
            cityRepository.saveAndFlush(city);
        }

        assertThat(directory.cities(county, " CITY ", 1, 2)).extracting(value -> value.name())
                .containsExactly("City 0", "City 1");
        for (String literal : List.of("%", "_", "!", "\\")) {
            assertThat(directory.cities(county, literal, 0, 50)).hasSize(1);
        }
        assertThat(directory.cities(county, "city", 100, 2)).isEmpty();
    }

    private UUID createSchool(String name, UUID cityId) {
        var school = new SchoolEntity();
        school.setName(name);
        school.setCity(cityId == null ? null : cityRepository.getReferenceById(cityId));
        return schoolRepository.saveAndFlush(school).getId();
    }
}
