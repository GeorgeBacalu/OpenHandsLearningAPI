package com.project.ohl;

import com.project.ohl.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.storage.s3.access-key=test-access", "app.storage.s3.secret-key=test-secret",
        "spring.servlet.multipart.max-file-size=1KB", "spring.servlet.multipart.max-request-size=4KB"})
class MediaIntegrationTest extends DatabaseIntegrationTest {
    @LocalServerPort private int port;
    @Autowired private JdbcTemplate database;
    @Autowired private PasswordHasher passwords;
    @MockitoBean private S3Client s3;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private String token;

    @BeforeEach
    void credentials() {
        token = null;
        database.update("UPDATE users SET password_hash = ?", passwords.hash("media-test-password"));
    }

    @Test
    void anyoneCanResolvePermanentPublicUrlsWithoutAwsCredentialsOrNetworkRequests() throws Exception {
        for (String type : new String[]{"images", "videos"}) {
            var response = send("GET", "/api/media/" + type + "?mediaKey=categories/alfabet/a." + (type.equals("images") ? "webp" : "mp4"), null, null);
            assertThat(response.statusCode()).isEqualTo(200);
            var media = json.readTree(response.body());
            assertThat(media.path("url").asString()).isEqualTo(
                    "https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/a." + (type.equals("images") ? "webp" : "mp4"));
            assertThat(media.has("expiresAt")).isFalse();
        }
        verifyNoInteractions(s3);
    }

    @Test
    void onlyAdministratorsCanUploadOrDelete() throws Exception {
        assertThat(upload("images", "a", "image/webp", "PNG").statusCode()).isEqualTo(401);
        assertThat(send("DELETE", "/api/media/images?mediaKey=categories/alfabet/a.webp", null, null).statusCode()).isEqualTo(401);
        login("ana@example.test");
        assertThat(upload("images", "a", "image/webp", "PNG").statusCode()).isEqualTo(403);
        assertThat(send("DELETE", "/api/media/images?mediaKey=categories/alfabet/a.webp", null, null).statusCode()).isEqualTo(403);
        verifyNoInteractions(s3);
    }

    @Test
    void administratorUploadsStreamToTheConfiguredBucketWithContentTypeAndNoAcl() throws Exception {
        login("dan@example.test");
        doAnswer(invocation -> {
            PutObjectRequest request = invocation.getArgument(0);
            RequestBody body = invocation.getArgument(1);
            assertThat(request.bucket()).isEqualTo("open-hands-learning");
            assertThat(request.key()).isIn("dev/categories/alfabet/a.webp", "dev/categories/alfabet/a.mp4");
            assertThat(request.contentLength()).isEqualTo(3L);
            assertThat(request.aclAsString()).isNull();
            try (var stream = body.contentStreamProvider().newStream()) {
                assertThat(stream.readAllBytes()).isEqualTo("ABC".getBytes(StandardCharsets.UTF_8));
            }
            return PutObjectResponse.builder().build();
        }).when(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertThat(upload("images", "a", "image/webp", "ABC").statusCode()).isEqualTo(204);
        assertThat(upload("videos", "a", "video/mp4", "ABC").statusCode()).isEqualTo(204);
        assertThat(send("DELETE", "/api/media/videos?mediaKey=categories/alfabet/a.mp4", null, null).statusCode()).isEqualTo(204);
        var deleted = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3).deleteObject(deleted.capture());
        assertThat(deleted.getValue().bucket()).isEqualTo("open-hands-learning");
        assertThat(deleted.getValue().key()).isEqualTo("dev/categories/alfabet/a.mp4");
    }

    @Test
    void invalidTypesKeysFilesAndOversizedUploadsNeverReachS3() throws Exception {
        assertThat(send("GET", "/api/media/documents?mediaKey=categories/alfabet/a.webp", null, null).statusCode()).isEqualTo(400);
        assertThat(send("GET", "/api/media/images?mediaKey=categories/alfabet/a.png", null, null).statusCode()).isEqualTo(400);
        login("dan@example.test");
        assertThat(upload("images", "a", "video/mp4", "ABC").statusCode()).isEqualTo(400);
        assertThat(upload("videos", "a", "text/html", "ABC").statusCode()).isEqualTo(400);
        assertThat(upload("images", "a", "image/webp", "").statusCode()).isEqualTo(400);
        assertThat(upload("images", "a", "image/webp", "A".repeat(2048)).statusCode()).isEqualTo(413);
        verifyNoInteractions(s3);
    }

    @Test
    void awsFailuresReturnSafeProblemDetails() throws Exception {
        login("dan@example.test");
        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().statusCode(403).message("private-provider-details").build());
        var response = upload("images", "a", "image/webp", "ABC");
        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(response.headers().firstValue("Content-Type").orElse("")).contains("application/problem+json");
        assertThat(response.body()).doesNotContain("private-provider-details");
    }

    @Test
    void categoriesReturnAndPersistImageUrlsAndRejectNonS3Urls() throws Exception {
        login("dan@example.test");
        String path = "/api/categories/" + TestIds.CATEGORY_PARENT;
        var category = json.readTree(send("GET", path, null, null).body());
        assertThat(category.has("mediaKey")).isFalse();
        assertThat(category.path("imageUrl").asString()).endsWith("/dev/categories/alfabet/alfabet.webp");
        String updated = "https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/new_image.webp";
        String body = "{\"name\":\"Alphabet\",\"imageUrl\":\"" + updated + "\"}";
        assertThat(send("PUT", path, "application/json", body).statusCode()).isEqualTo(200);
        assertThat(database.queryForObject("SELECT image_url FROM categories WHERE id = ?", String.class,
                TestIds.CATEGORY_PARENT)).isEqualTo(updated);
        String video = "https://open-hands-learning.s3.us-east-1.amazonaws.com/dev/categories/alfabet/demo.mp4";
        assertThat(send("PUT", path, "application/json", "{\"name\":\"Alphabet\",\"videoUrl\":\"" + video + "\"}").statusCode()).isEqualTo(200);
        assertThat(send("PUT", path, "application/json", "{\"name\":\"Renamed\"}").statusCode()).isEqualTo(200);
        assertThat(json.readTree(send("GET", path, null, null).body()).path("videoUrl").asString()).isEqualTo(video);
        assertThat(json.readTree(send("GET", path, null, null).body()).path("imageUrl").asString()).isEqualTo(updated);
        assertThat(send("PUT", path, "application/json", body.replace(updated, "https://example.com/image")).statusCode())
                .isEqualTo(400);
    }

    private void login(String email) throws Exception {
        var response = send("POST", "/api/auth/login", "application/json",
                "{\"email\":\"" + email + "\",\"password\":\"media-test-password\"}");
        assertThat(response.statusCode()).isEqualTo(200);
        token = json.readTree(response.body()).path("accessToken").asString();
    }

    private HttpResponse<String> upload(String type, String key, String contentType, String content) throws Exception {
        String body = "--media-boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"test\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n" + content + "\r\n--media-boundary--\r\n";
        return send("PUT", "/api/media/" + type + "?mediaKey=categories/alfabet/" + key + (type.equals("images") ? ".webp" : ".mp4"), "multipart/form-data; boundary=media-boundary", body);
    }

    private HttpResponse<String> send(String method, String path, String contentType, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(30));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (contentType != null) request.header("Content-Type", contentType);
        return client.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
