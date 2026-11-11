package com.project.ohl;

import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class DatabaseIntegrationTest {
    private static final String[] TABLES = {"favorites", "schools", "roles", "sections", "categories",
            "users", "counties", "cities"};
    @Autowired private DataSource dataSource;

    @BeforeEach
    final void seedDatabase() {
        clearDatabase();
        execute("test.sql");
        assertRowCounts(2);
    }

    @AfterEach
    final void clearDatabase() {
        execute("cleanup.sql");
        assertRowCounts(0);
    }

    private void execute(String script) {
        new ResourceDatabasePopulator(new ClassPathResource(script)).execute(dataSource);
    }

    private void assertRowCounts(long expected) {
        var database = new JdbcTemplate(dataSource);
        for (String table : TABLES) {
            assertThat(database.queryForObject("SELECT COUNT(*) FROM " + table, Long.class))
                    .as("Rows in %s", table).isEqualTo(expected);
        }
    }
}
