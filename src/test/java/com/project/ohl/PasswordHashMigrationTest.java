package com.project.ohl;

import db.migration.V16__rename_password_to_password_hash;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;

import static com.project.ohl.TestIds.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordHashMigrationTest {
    private record Fixture(JdbcTemplate database, Flyway flyway) {}

    @Test
    void hashesLegacyPasswordsWithoutChangingExistingHashesOrRelationships() {
        var fixture = fixture();
        var database = fixture.database();
        String first = "legacy-password-one";
        String second = "parolă-veche-cu-diacritice";
        String existing = new BCryptPasswordEncoder(BCryptVersion.$2A, 4).encode("already-hashed-password");
        insertUser(database, 501, first);
        insertUser(database, 502, second);
        insertUser(database, 503, existing);
        database.update("INSERT INTO favorites (user_id, category_id) VALUES (?, ?), (?, ?)",
                id(501), id(500), id(503), id(500));
        var users = database.queryForList("SELECT id, name, email, phone, role_id, school_id FROM users ORDER BY id");
        var favorites = database.queryForList("SELECT user_id, category_id FROM favorites ORDER BY user_id");

        assertThat(fixture.flyway().migrate().migrationsExecuted).isEqualTo(1);

        assertThat(columns(database)).contains("password_hash").doesNotContain("password");
        assertThat(database.queryForList("SELECT id, name, email, phone, role_id, school_id FROM users ORDER BY id"))
                .isEqualTo(users);
        assertThat(database.queryForList("SELECT user_id, category_id FROM favorites ORDER BY user_id"))
                .isEqualTo(favorites);
        var encoder = new BCryptPasswordEncoder();
        for (var entry : Map.of(501, first, 502, second).entrySet()) {
            String hash = hash(database, entry.getKey());
            assertThat(hash).startsWith("$2b$12$");
            assertThat(encoder.matches(entry.getValue(), hash)).isTrue();
        }
        assertThat(hash(database, 503)).isEqualTo(existing);
        assertThat(fixture.flyway().migrate().migrationsExecuted).isZero();
        assertThat(fixture.flyway().validateWithResult().validationSuccessful).isTrue();
        assertThat(hash(database, 501)).isNotEqualTo(first);
        assertThat(hash(database, 503)).isEqualTo(existing);
    }

    @Test
    void rejectsOversizedUtf8PasswordsBeforeChangingAnyCredentialOrColumn() {
        var fixture = fixture();
        var database = fixture.database();
        insertUser(database, 501, "valid-legacy-password");
        insertUser(database, 502, "é".repeat(37));
        var before = database.queryForList("SELECT * FROM users ORDER BY id");

        assertThatThrownBy(() -> fixture.flyway().migrate())
                .hasStackTraceContaining("Legacy password exceeds BCrypt's 72-byte limit.");

        assertThat(columns(database)).contains("password").doesNotContain("password_hash");
        assertThat(database.queryForList("SELECT * FROM users ORDER BY id")).isEqualTo(before);
    }

    private static Fixture fixture() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:password-hash-" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        var database = new JdbcTemplate(source);
        database.execute("CREATE TABLE roles (id UUID PRIMARY KEY)");
        database.execute("CREATE TABLE schools (id UUID PRIMARY KEY)");
        database.execute("CREATE TABLE categories (id UUID PRIMARY KEY)");
        database.execute("CREATE TABLE users (id UUID PRIMARY KEY, name VARCHAR(100) NOT NULL,"
                + " email VARCHAR(254) NOT NULL, password VARCHAR(255) NOT NULL, phone VARCHAR(10) NOT NULL,"
                + " role_id UUID NOT NULL REFERENCES roles(id), school_id UUID NOT NULL REFERENCES schools(id))");
        database.execute("CREATE TABLE favorites (user_id UUID REFERENCES users(id),"
                + " category_id UUID REFERENCES categories(id), PRIMARY KEY (user_id, category_id))");
        for (String table : new String[]{"roles", "schools", "categories"})
            database.update("INSERT INTO " + table + " (id) VALUES (?)", id(500));
        var flyway = Flyway.configure().dataSource(source).locations(new String[0])
                .javaMigrations(new V16__rename_password_to_password_hash())
                .baselineOnMigrate(true).baselineVersion("15").load();
        return new Fixture(database, flyway);
    }

    private static void insertUser(JdbcTemplate database, int number, String password) {
        database.update("INSERT INTO users (id, name, email, password, phone, role_id, school_id)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)", id(number), "Legacy user " + number,
                "legacy-" + number + "@example.test", password, "0700000000", id(500), id(500));
    }

    private static List<String> columns(JdbcTemplate database) {
        return database.queryForList("SELECT column_name FROM information_schema.columns"
                + " WHERE table_name = 'users' AND table_schema = 'public'", String.class);
    }

    private static String hash(JdbcTemplate database, int number) {
        return database.queryForObject("SELECT password_hash FROM users WHERE id = ?", String.class, id(number));
    }
}
