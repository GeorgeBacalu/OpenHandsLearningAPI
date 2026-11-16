package com.project.ohl;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageUrlMigrationTest {
    @Test
    void migratesExistingKeysWithoutChangingCategoryIdentityHierarchyOrFavorites() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:image-urls-" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        var database = new JdbcTemplate(source);
        database.execute("CREATE TABLE categories (id UUID PRIMARY KEY, name VARCHAR(255),"
                + " parent_id UUID REFERENCES categories(id), media_key VARCHAR(255) NOT NULL,"
                + " CONSTRAINT categories_media_key_valid CHECK (media_key ~ '^[a-z0-9_]+$'))");
        database.execute("CREATE TABLE favorites (user_id UUID, category_id UUID REFERENCES categories(id))");
        database.update("INSERT INTO categories VALUES (?, 'Alphabet', NULL, 'alfabet')", TestIds.CATEGORY_PARENT);
        database.update("INSERT INTO categories VALUES (?, 'Letter A', ?, 'a')", TestIds.CATEGORY_CHILD, TestIds.CATEGORY_PARENT);
        database.update("INSERT INTO favorites VALUES (?, ?)", TestIds.USER_ANA, TestIds.CATEGORY_CHILD);
        var before = database.queryForList("SELECT id, name, parent_id FROM categories ORDER BY id");
        var favorites = database.queryForList("SELECT * FROM favorites");
        var flyway = Flyway.configure().dataSource(source).baselineOnMigrate(true).baselineVersion("13").target("14").load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        assertThat(database.queryForList("SELECT id, name, parent_id FROM categories ORDER BY id")).isEqualTo(before);
        assertThat(database.queryForList("SELECT * FROM favorites")).isEqualTo(favorites);
        assertThat(database.queryForObject("SELECT image_url FROM categories WHERE id = ?", String.class, TestIds.CATEGORY_PARENT))
                .isEqualTo("https://open-hands-learning.s3.us-east-1.amazonaws.com/images/alfabet");
        assertThat(database.queryForList("SELECT column_name FROM information_schema.columns WHERE table_name = 'categories'", String.class))
                .contains("image_url").doesNotContain("media_key");
        assertThatThrownBy(() -> database.update("UPDATE categories SET image_url = 'http://example.com/a'"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
    }
}
