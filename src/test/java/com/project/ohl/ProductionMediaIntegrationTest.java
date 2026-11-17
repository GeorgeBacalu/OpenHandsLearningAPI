package com.project.ohl;

import com.project.ohl.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ActiveProfiles(value = {"prod", "test"}, inheritProfiles = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "NEON_STORAGE_ACCESS_KEY_ID=test-neon-access", "NEON_STORAGE_SECRET_ACCESS_KEY=test-neon-secret"})
class ProductionMediaIntegrationTest extends DatabaseIntegrationTest {
    private static final String KEY = "categories/deployment_checks/profile/main.webp";
    @LocalServerPort private int port;
    @Autowired private PasswordHasher passwords;
    @MockitoBean private S3Client storage;
    private final HttpClient http = HttpClient.newHttpClient();
    private String token;

    @Test
    void productionMediaEndpointsUseTheNeonBucketAndProdPrefixAndStillRequireAdministratorAccess() throws Exception {
        String route = "/api/media/images?mediaKey=" + KEY;
        var resolved = send("GET", route, null, null);
        assertThat(resolved.statusCode()).isEqualTo(200);
        assertThat(resolved.body()).contains("https://br-red-base-b9fi1yrl.storage.c-7.eu-central-1.aws.neon.tech/openhandslearning-media/" + KEY);
        assertThat(send("DELETE", route, null, null).statusCode()).isEqualTo(401);
        verifyNoInteractions(storage);

        setFixturePassword(passwords.hash("production-media-test"));
        var login = send("POST", "/api/auth/login", "application/json",
                "{\"email\":\"dan@example.test\",\"password\":\"production-media-test\"}");
        assertThat(login.statusCode()).isEqualTo(200);
        token = JsonMapper.builder().build().readTree(login.body()).path("accessToken").asString();
        String body = "--boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"sample.webp\"\r\n"
                + "Content-Type: image/webp\r\n\r\nABC\r\n--boundary--\r\n";
        assertThat(send("PUT", route, "multipart/form-data; boundary=boundary", body).statusCode()).isEqualTo(204);
        assertThat(send("DELETE", route, null, null).statusCode()).isEqualTo(204);

        var uploaded = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(storage).putObject(uploaded.capture(), any(RequestBody.class));
        assertThat(uploaded.getValue().bucket()).isEqualTo("openhandslearning-media");
        assertThat(uploaded.getValue().key()).isEqualTo(KEY);
        assertThat(uploaded.getValue().aclAsString()).isNull();
        var deleted = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(storage).deleteObject(deleted.capture());
        assertThat(deleted.getValue().bucket()).isEqualTo("openhandslearning-media");
        assertThat(deleted.getValue().key()).isEqualTo(KEY);
    }

    private HttpResponse<String> send(String method, String route, String contentType, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + route)).timeout(Duration.ofSeconds(30));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (contentType != null) request.header("Content-Type", contentType);
        return http.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
