package com.project.ohl.adapter.storage;

import com.project.ohl.application.service.MediaServiceImpl;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/** Explicitly opt in: writes/deletes only its own random deployment_checks object. */
@EnabledIfEnvironmentVariable(named = "OHL_MEDIA_STORAGE_SMOKE", matches = "true")
class MediaStorageSmokeTest {
    @Test void neonProductionRoundTrip() { roundTrip("prod"); }
    @Test void awsDevelopmentRoundTrip() { roundTrip("dev"); }

    private void roundTrip(String profile) {
        new ApplicationContextRunner()
                .withInitializer(app -> {
                    app.getEnvironment().setActiveProfiles(profile);
                    new ConfigDataApplicationContextInitializer().initialize(app);
                })
                .withPropertyValues("spring.config.location=file:src/main/resources/application.properties")
                .withBean(PropertySourcesPlaceholderConfigurer.class)
                .withUserConfiguration(S3StorageConfig.class, S3MediaStorageAdapter.class, MediaServiceImpl.class)
                .run(app -> {
                    assertThat(app).hasNotFailed();
                    var media = app.getBean(MediaServiceImpl.class);
                    String key = "categories/deployment_checks/" + UUID.randomUUID().toString().replace("-", "") + "/main.webp";
                    byte[] content = "Open Hands Learning storage smoke check".getBytes(StandardCharsets.UTF_8);
                    var url = URI.create(media.download("images", key).url());
                    try (var http = HttpClient.newHttpClient()) {
                        try {
                            media.upload("images", key, "image/webp", content.length, new ByteArrayInputStream(content));
                            var fetched = http.send(HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(45)).GET().build(),
                                    HttpResponse.BodyHandlers.ofByteArray());
                            assertThat(fetched.statusCode()).isEqualTo(200);
                            assertThat(fetched.body()).isEqualTo(content);
                        } finally {
                            media.delete("images", key);
                        }
                        var deleted = http.send(HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(45)).GET().build(),
                                HttpResponse.BodyHandlers.discarding());
                        assertThat(deleted.statusCode()).isIn(403, 404);
                        System.out.println("Verified " + profile + " upload, public read, and delete; temporary object removed.");
                    }
                });
    }
}
