package com.project.ohl;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationTest {
    @Test
    void createsTheSchemaWithoutSeedsAndAppliesEachVersionOnlyOnce() {
        var source = new DriverManagerDataSource(
                "jdbc:h2:mem:flyway-schema-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        var config = Flyway.configure().dataSource(source).defaultSchema("public").target("1");
        var flyway = config.load();
        flyway.migrate();
        assertThat(flyway.info().applied()).hasSize(1);
        var database = new JdbcTemplate(source);
        var tables = database.queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' AND table_name <> 'flyway_schema_history'", String.class);
        assertThat(tables).isNotEmpty();
        for (String table : tables)
            assertThat(database.queryForObject("SELECT COUNT(*) FROM " + table, Long.class)).as(table).isZero();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
    }
}
