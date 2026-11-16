package com.project.ohl;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HierarchicalMediaMigrationTest {
    @Test
    void preservesCatalogDuringTransitionAndStoresVoiceAndVideoPublicUrls() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:hierarchical-media-" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        var database = new JdbcTemplate(source);
        database.execute("CREATE TABLE categories (id UUID PRIMARY KEY, name VARCHAR(255), parent_id UUID,"
                + " image_url VARCHAR(2048) NOT NULL, CONSTRAINT categories_image_url_valid CHECK (image_url IS NOT NULL))");
        String oldUrl = "https://open-hands-learning.s3.us-east-1.amazonaws.com/images/a";
        database.update("INSERT INTO categories VALUES (?, 'A', NULL, ?)", TestIds.CATEGORY_CHILD, oldUrl);
        var before = database.queryForList("SELECT * FROM categories");
        var flyway = Flyway.configure().dataSource(source).baselineOnMigrate(true).baselineVersion("14").target("15").load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        assertThat(database.queryForList("SELECT id, name, parent_id, image_url FROM categories")).isEqualTo(before);
        String base = "https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/";
        database.update("UPDATE categories SET image_url=?, video_url=?", base + "categories/alfabet/a.webp", base + "categories/alfabet/a.mp4");
        database.update("INSERT INTO media_assets VALUES (?, ?, 'image/webp', 10, ?)",
                "dev/voice/voice_a.webp", base + "voice/voice_a.webp", "a".repeat(64));
        assertThatThrownBy(() -> database.update("UPDATE categories SET video_url=?", base + "voice/a.mp4"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> database.update("UPDATE media_assets SET public_url=?", "https://example.test/a.webp"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
    }
}
