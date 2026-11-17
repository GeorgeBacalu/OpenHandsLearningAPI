package com.project.ohl;

import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3RootMediaMigrationTest {
    @Test
    void migratesOnlyTheMovedBucketAndAllowsRootUrls() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:s3-root-migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
                var sql = connection.createStatement()) {
            String aws = "https://open-hands-learning.s3.us-east-1.amazonaws.com/";
            String neon = "https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/prod/";
            sql.execute("CREATE TABLE categories (id int PRIMARY KEY, image_url varchar(2048), video_url varchar(2048), "
                    + "CONSTRAINT categories_image_url_valid CHECK (image_url IS NOT NULL), "
                    + "CONSTRAINT categories_video_url_valid CHECK (video_url IS NULL OR video_url IS NOT NULL))");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/migration/V17__allow_free_neon_media.sql"));
            sql.execute("INSERT INTO categories VALUES (1, '" + aws + "dev/categories/alfabet/main.webp', '" + aws + "dev/categories/alfabet/a/main.mp4'),"
                    + "(2, '" + neon + "categories/alfabet/main.webp', '" + neon + "categories/alfabet/a/main.mp4'),"
                    + "(3, '" + aws + "prod/categories/alfabet/main.webp', NULL)");

            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/migration/V18__use_root_s3_media_paths.sql"));

            try (var rows = sql.executeQuery("SELECT * FROM categories ORDER BY id")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getInt("id")).isEqualTo(1);
                assertThat(rows.getString("image_url")).isEqualTo(aws + "categories/alfabet/main.webp");
                assertThat(rows.getString("video_url")).isEqualTo(aws + "categories/alfabet/a/main.mp4");
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("image_url")).isEqualTo(neon + "categories/alfabet/main.webp");
                assertThat(rows.getString("video_url")).isEqualTo(neon + "categories/alfabet/a/main.mp4");
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("image_url")).isEqualTo(aws + "prod/categories/alfabet/main.webp");
                assertThat(rows.getString("video_url")).isNull();
                assertThat(rows.next()).isFalse();
            }
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/migration/V19__use_root_neon_media_paths.sql"));
            try (var rows = sql.executeQuery("SELECT image_url, video_url FROM categories WHERE id = 2")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString("image_url")).isEqualTo(neon.replace("/prod/", "/") + "categories/alfabet/main.webp");
                assertThat(rows.getString("video_url")).isEqualTo(neon.replace("/prod/", "/") + "categories/alfabet/a/main.mp4");
            }
            sql.execute("INSERT INTO categories VALUES (4, '" + aws + "categories/alfabet/a/main.webp', NULL)");
            assertThatThrownBy(() -> sql.execute("UPDATE categories SET image_url = '" + aws + "categories/alfabet/main.html' WHERE id = 4"))
                    .isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> sql.execute("UPDATE categories SET video_url = '" + aws + "categories/alfabet/main.webp' WHERE id = 4"))
                    .isInstanceOf(java.sql.SQLException.class);
        }
    }
}
