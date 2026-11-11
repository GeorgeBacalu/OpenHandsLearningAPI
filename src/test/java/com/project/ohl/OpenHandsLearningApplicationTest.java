package com.project.ohl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.json.JsonMapper;

import static com.project.ohl.TestIds.INSTITUTION_A;
import static com.project.ohl.TestIds.CATEGORY_CHILD;
import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.SECTION_VIDEO;
import static com.project.ohl.TestIds.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenHandsLearningApplicationTest extends DatabaseIntegrationTest {
    @LocalServerPort private int port;
    @Autowired private JdbcTemplate database;
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
        var users = json.readTree(response.body());
        assertThat(users.size()).isEqualTo(2);
        assertThat(List.of(users.get(0).path("name").asText(), users.get(1).path("name").asText()))
                .containsExactlyInAnyOrder("Ana", "Dan");
        var ana = users.get(0).path("name").asText().equals("Ana") ? users.get(0) : users.get(1);
        assertThat(ana.path("mobile").asText()).isEqualTo("0712345678");
        assertThat(users.get(0).has("password")).isFalse();
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
        var missing = send("GET", "/api/users/00000000-0000-4000-8000-0000000f423f", null);
        assertThat(missing.statusCode()).isEqualTo(404);
        assertThat(missing.headers().firstValue("Content-Type").orElse("")).contains("application/problem+json");
        assertThat(json.readTree(missing.body()).path("status").asInt()).isEqualTo(404);
        assertThat(send("GET", "/api/users/not-a-uuid", null).statusCode()).isEqualTo(400);
        var invalid = send("POST", "/api/users", "{}");
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(json.readTree(invalid.body()).path("errors").has("password")).isTrue();
        assertThat(send("PATCH", "/api/users", "{}").statusCode()).isEqualTo(405);
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users"))
                .header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.ofString("invalid")).build();
        assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(415);
    }

    @Test
    void returnsBadRequestForPasswordsExceedingBcryptUtf8Limit() throws Exception {
        var request = json.createObjectNode();
        request.put("name", "Test User");
        request.put("email", "test@example.test");
        request.put("password", "\u00e9".repeat(37));
        request.put("mobile", "0712345678");
        request.put("address", "Bucharest");
        request.put("institutionId", INSTITUTION_A.toString());
        request.put("roleId", ROLE_USER.toString());
        var response = send("POST", "/api/users", json.writeValueAsString(request));
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(json.readTree(response.body()).path("detail").asText())
                .isEqualTo("Password must not exceed 72 UTF-8 bytes.");
    }

    @Test
    void eventListsAreBoundedFilteredAndOrdered() throws Exception {
        var first = json.readTree(send("GET", "/api/notifications?size=1", null).body());
        var second = json.readTree(send("GET", "/api/notifications?page=1&size=1", null).body());
        assertThat(first.size()).isEqualTo(1);
        assertThat(second.size()).isEqualTo(1);
        assertThat(first.get(0).path("id").asText()).isNotEqualTo(second.get(0).path("id").asText());
        assertThat(Instant.parse(first.get(0).path("sentAt").asText()))
                .isAfter(Instant.parse(second.get(0).path("sentAt").asText()));
        assertThat(json.readTree(send("GET", "/api/notifications?page=2&size=1", null).body()).size()).isZero();
        String receiver = first.get(0).path("receiverId").asText();
        var inbox = json.readTree(send("GET", "/api/notifications?receiverId=" + receiver, null).body());
        assertThat(inbox.size()).isEqualTo(1);
        assertThat(inbox.get(0).path("receiverId").asText()).isEqualTo(receiver);
        var feedback = json.readTree(send("GET", "/api/feedbacks?size=1", null).body());
        String user = feedback.get(0).path("userId").asText();
        var filtered = json.readTree(send("GET", "/api/feedbacks?userId=" + user, null).body());
        assertThat(filtered.size()).isEqualTo(1);
        assertThat(filtered.get(0).path("userId").asText()).isEqualTo(user);
        assertThat(send("GET", "/api/notifications?size=101", null).statusCode()).isEqualTo(400);
        assertThat(send("GET", "/api/feedbacks?page=-1", null).statusCode()).isEqualTo(400);
    }

    @Test
    void feedbackSupportsLongTextWithAnExplicitLimitAndUtcTimestamp() throws Exception {
        var users = json.readTree(send("GET", "/api/users", null).body());
        var request = json.createObjectNode().put("type", "ISSUE").put("description", "x".repeat(1000))
                .put("userId", users.get(0).path("id").asText());
        var response = send("POST", "/api/feedbacks", json.writeValueAsString(request));
        assertThat(response.statusCode()).isEqualTo(201);
        var saved = json.readTree(response.body());
        assertThat(saved.path("description").asText()).hasSize(1000);
        assertThat(saved.path("sentAt").asText()).endsWith("Z");
        assertThat(json.readTree(send("GET", "/api/feedbacks/" + saved.path("id").asText(), null).body())
                .path("description").asText()).hasSize(1000);
        request.put("description", "x".repeat(10001));
        assertThat(send("POST", "/api/feedbacks", json.writeValueAsString(request)).statusCode()).isEqualTo(400);
        var notice = json.createObjectNode().put("message", "x".repeat(1000)).put("isRead", false)
                .put("senderId", users.get(0).path("id").asText())
                .put("receiverId", users.get(1).path("id").asText());
        assertThat(send("POST", "/api/notifications", json.writeValueAsString(notice)).statusCode()).isEqualTo(201);
        notice.put("message", "x".repeat(10001));
        assertThat(send("POST", "/api/notifications", json.writeValueAsString(notice)).statusCode()).isEqualTo(400);
    }

    @Test
    void letterSignCrudIsNoLongerExposed() throws Exception {
        assertThat(send("GET", "/api/letter-sign-pairs", null).statusCode()).isEqualTo(404);
        var contract = json.readTree(send("GET", "/openapi.json", null).body());
        assertThat(contract.path("paths").has("/api/letter-sign-pairs")).isFalse();
        assertThat(contract.path("components").path("schemas").has("LetterSignPairDto")).isFalse();
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30)).header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    @Test
    void categoryPagesKeepParentsAndChildrenSeparateAndAlphabetical() throws Exception {
        database.update("UPDATE categories SET name = ? WHERE id = ?", "Zebră", CATEGORY_PARENT);
        database.update("UPDATE categories SET name = ? WHERE id = ?", "Șarpe", CATEGORY_CHILD);
        var request = json.createObjectNode().put("name", "Arici")
                .put("sectionId", SECTION_VIDEO.toString()).put("mediaKey", "arici");
        var created = send("POST", "/api/categories", json.writeValueAsString(request));
        assertThat(created.statusCode()).isEqualTo(201);
        String id = json.readTree(created.body()).path("id").asText();

        var roots = json.readTree(send("GET", "/api/categories/filter?sectionId=" + SECTION_VIDEO, null).body());
        assertThat(roots.size()).isEqualTo(2);
        assertThat(roots.get(0).has("code")).isFalse();
        assertThat(send("GET", "/api/categories/filter", null).statusCode()).isEqualTo(400);
        assertThat(List.of(roots.get(0).path("name").asText(), roots.get(1).path("name").asText()))
                .containsExactly("Arici", "Zebră");
        assertThat(roots.get(0).path("parentId").isNull()).isTrue();
        assertThat(roots.get(1).path("parentId").isNull()).isTrue();
        var searchedRoots = json.readTree(send("GET", "/api/categories/filter?sectionId=" + SECTION_VIDEO + "&name=ici", null).body());
        assertThat(searchedRoots.size()).isEqualTo(1);
        assertThat(searchedRoots.get(0).path("name").asText()).isEqualTo("Arici");

        request.put("name", "Țară").put("parentId", CATEGORY_PARENT.toString());
        assertThat(send("PUT", "/api/categories/" + id, json.writeValueAsString(request)).statusCode()).isEqualTo(200);
        var children = json.readTree(send("GET", "/api/categories/filter?parentId=" + CATEGORY_PARENT
                + "&sectionId=" + SECTION_VIDEO, null).body());
        assertThat(children.size()).isEqualTo(2);
        assertThat(List.of(children.get(0).path("name").asText(), children.get(1).path("name").asText()))
                .containsExactly("Șarpe", "Țară");
        assertThat(children.get(0).path("parentId").asText()).isEqualTo(CATEGORY_PARENT.toString());
        assertThat(children.get(1).path("parentId").asText()).isEqualTo(CATEGORY_PARENT.toString());
        var searchedChildren = json.readTree(send("GET",
                "/api/categories/filter?parentId=" + CATEGORY_PARENT + "&sectionId=" + SECTION_VIDEO + "&name=pe", null).body());
        assertThat(searchedChildren.size()).isEqualTo(1);
        assertThat(searchedChildren.get(0).path("name").asText()).isEqualTo("Șarpe");
        UUID ana = database.queryForObject("SELECT id FROM users WHERE name = 'Ana'", UUID.class);
        assertThat(send("PATCH", "/api/categories/favorites/save?userId=" + ana + "&categoryId=" + id, null)
                .statusCode()).isEqualTo(200);
        var favorites = json.readTree(send("GET", "/api/categories/favorites?userId=" + ana, null).body());
        assertThat(favorites.size()).isEqualTo(2);
        assertThat(List.of(favorites.get(0).path("name").asText(), favorites.get(1).path("name").asText()))
                .containsExactly("Șarpe", "Țară");
    }

}
