package com.project.ohl.directory;

import com.project.ohl.DatabaseIntegrationTest;
import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.adapter.web.SchoolDirectoryController;
import com.project.ohl.domain.exception.InvalidRequestException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SchoolDirectoryIntegrationTest extends DatabaseIntegrationTest {
    @Autowired SchoolDirectoryPort directory;
    @Autowired SchoolDirectoryController controller;
    @Autowired JdbcTemplate database;

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
        database.update("INSERT INTO schools (id, name, city_id) VALUES (?, ?, ?)", UUID.randomUUID(), "School B", city);
        assertThat(directory.schools(city, "School B", 0, 50)).hasSize(1);
        assertThatThrownBy(() -> database.update("INSERT INTO schools (id, name, city_id) VALUES (?, ?, ?)",
                UUID.randomUUID(), "Invalid school", UUID.randomUUID())).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> database.update("INSERT INTO schools (id, name, city_id) VALUES (?, ?, NULL)",
                UUID.randomUUID(), "Missing city")).isInstanceOf(DataIntegrityViolationException.class);
    }
}
