package com.project.ohl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenHandsLearningApplicationTest {
    @LocalServerPort private int port;
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void servesJsonFromTheDatabase() throws Exception {
        var response = send("GET", "/api/users", null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type").orElse("")).contains("application/json");
        assertThat(response.body()).isEqualTo("[]");
    }

    @Test
    void checkedInContractMatchesEveryHttpOperation() throws Exception {
        var response = send("GET", "/openapi.json", null);
        assertThat(response.statusCode()).isEqualTo(200);
        var contract = json.readTree(response.body());
        assertThat(contract.path("openapi").asText()).isEqualTo("3.2.1");
        var documented = new HashSet<String>();
        contract.path("paths").properties().forEach(path -> path.getValue().properties().forEach(operation ->
                documented.add(operation.getKey().toUpperCase() + " " + path.getKey())));
        var implemented = new HashSet<String>();
        mappings.getHandlerMethods().forEach((mapping, handler) -> mapping.getPatternValues().stream()
                .filter(path -> path.startsWith("/api/"))
                .forEach(path -> mapping.getMethodsCondition().getMethods()
                        .forEach(method -> implemented.add(method.name() + " " + path))));
        assertThat(documented).containsExactlyInAnyOrderElementsOf(implemented);
        assertThat(contract.path("components").path("schemas").path("UserResponse").path("properties").has("password"))
                .isFalse();
    }

    @Test
    void returnsProblemDetailsForMissingResourcesAndInvalidRequests() throws Exception {
        var missing = send("GET", "/api/users/999999", null);
        assertThat(missing.statusCode()).isEqualTo(404);
        assertThat(missing.headers().firstValue("Content-Type").orElse("")).contains("application/problem+json");
        assertThat(json.readTree(missing.body()).path("status").asInt()).isEqualTo(404);
        var invalid = send("POST", "/api/users", "{}");
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(json.readTree(invalid.body()).path("errors").has("password")).isTrue();
        assertThat(send("PATCH", "/api/users", "{}").statusCode()).isEqualTo(405);
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users"))
                .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.ofString("invalid")).build();
        assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(415);
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30)).header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
