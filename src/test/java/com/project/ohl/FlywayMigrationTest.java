package com.project.ohl;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flyway-schema-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.flyway.enabled=true",
        "spring.flyway.default-schema=public",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FlywayMigrationTest {
    @Autowired private Flyway flyway;
    @Autowired private JdbcTemplate database;

    @Test
    void createsTheSchemaWithoutRecordsAndAppliesItOnlyOnce() {
        var applied = flyway.info().applied();
        assertThat(applied).hasSize(1);
        assertThat(applied[0].getVersion().getVersion()).isEqualTo("1");
        for (String table : new String[]{"categories", "feedbacks", "institutions", "letter_sign_pairs",
                "notifications", "roles", "sections", "users"}) {
            assertThat(database.queryForObject("SELECT COUNT(*) FROM " + table, Long.class))
                    .as("Migration must not seed %s", table).isZero();
        }
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
    }
}
