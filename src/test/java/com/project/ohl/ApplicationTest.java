package com.project.ohl;

import com.project.ohl.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.json.JsonMapper;

import static com.project.ohl.TestIds.CATEGORY_CHILD;
import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.SCHOOL_A;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest extends DatabaseIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    private HttpClient client;

    private String token;

    @Autowired
    private PasswordHasher passwords;

    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void authenticateFixture() throws Exception {
        setFixturePassword(passwords.hash("correct-test-password"));
        client = HttpClient.newHttpClient();
        token = null;

        authenticate("dan@example.test");
    }

    private void authenticate(String email) throws Exception {
        String credentials = json.writeValueAsString(json.createObjectNode().put("email", email).put("password", "correct-test-password"));

        var response = send("POST", "/api/auth/login", credentials);

        assertThat(response.statusCode()).isEqualTo(200);

        token = json.readTree(response.body()).path("accessToken").asString();
    }

    @Test
    void servesJsonFromTheDatabase() throws Exception {
        var response = send("GET", "/api/users", null);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type").orElse("")).contains("application/json");

        var users = json.readTree(response.body());

        assertThat(users.size()).isEqualTo(2);
        assertThat(List.of(users.get(0).path("name").asString(), users.get(1).path("name").asString()))
                .containsExactlyInAnyOrder("Ana", "Dan");

        var ana = users.get(0).path("name").asString().equals("Ana") ? users.get(0) : users.get(1);

        assertThat(ana.path("phone").asString()).isEqualTo("0712345678");
        assertThat(users.get(0).has("password")).isFalse();
    }

    @Test
    void sectionsAreLocalToTheAppAndHaveNoApi() throws Exception {
        assertThat(send("GET", "/api/sections", null).statusCode()).isEqualTo(404);
        assertThat(send("GET", "/openapi.json", null).body()).doesNotContain("/api/sections", "SectionDto");
    }

    @Test
    void userProfilesContainOnlyTheSupportedFields() throws Exception {
        var columns = nativeValues("SELECT LOWER(column_name) FROM information_schema.columns WHERE LOWER(table_schema) = 'public' AND LOWER(table_name) = 'users'", String.class);

        assertThat(columns).containsExactlyInAnyOrder("id", "name", "email", "phone", "password_hash", "role", "school_id");

        var request = json.createObjectNode().put("name", "Created profile").put("email", "created-profile@example.test")
                .put("phone", "0712345678").put("password", "test-password")
                .put("role", "USER").put("schoolId", SCHOOL_A.toString());
        var created = send("POST", "/api/users", json.writeValueAsString(request));

        assertThat(created.statusCode()).isEqualTo(201);

        var profile = json.readTree(created.body());

        assertThat(profile.size()).isEqualTo(6);
        assertThat(profile.path("phone").asString()).isEqualTo("0712345678");
        assertThat(profile.path("role").asString()).isEqualTo("USER");
        assertThat(profile.has("password") || profile.has("mobile") || profile.has("address") || profile.has("birthday")).isFalse();

        String id = profile.path("id").asString();

        request.put("phone", "0798765432");

        assertThat(send("PUT", "/api/users/" + id, json.writeValueAsString(request)).statusCode()).isEqualTo(200);
        assertThat(json.readTree(send("GET", "/api/users/" + id, null).body()).path("phone").asString()).isEqualTo("0798765432");
        assertThat(send("DELETE", "/api/users/" + id, null).statusCode()).isEqualTo(204);
    }

    @Test
    void roleValuesAreValidatedAndRoleManagementIsRemoved() throws Exception {
        var request = json.createObjectNode().put("name", "Admin profile").put("email", "admin-profile@example.test")
                .put("phone", "0712345678").put("password", "test-password")
                .put("role", "OWNER").put("schoolId", SCHOOL_A.toString());

        assertThat(send("POST", "/api/users", json.writeValueAsString(request)).statusCode()).isEqualTo(400);

        request.putNull("role");

        assertThat(send("POST", "/api/users", json.writeValueAsString(request)).statusCode()).isEqualTo(400);

        request.put("role", "ADMIN");

        var response = send("POST", "/api/users", json.writeValueAsString(request));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(json.readTree(response.body()).path("role").asString()).isEqualTo("ADMIN");
        assertThat(userRepository.findByEmail("admin-profile@example.test").orElseThrow().getRole().name())
                .isEqualTo("ADMIN");
        assertThat(send("GET", "/api/roles", null).statusCode()).isEqualTo(404);
        assertThat(send("POST", "/api/roles", "{}").statusCode()).isEqualTo(404);
        assertThat(send("GET", "/openapi.json", null).body()).doesNotContain("/api/roles", "RoleDto", "roleId");
    }

    @Test
    void classroomManagementAndAssignmentsAreRemoved() throws Exception {
        assertThat(send("GET", "/api/classrooms", null).statusCode()).isEqualTo(404);
        assertThat(send("GET", "/api/classrooms/" + UUID.randomUUID(), null).statusCode()).isEqualTo(404);

        String contract = send("GET", "/openapi.json", null).body();

        assertThat(contract).doesNotContain("/api/classrooms", "ClassroomDto", "classroomId");

        var users = json.readTree(send("GET", "/api/users", null).body());
        for (var user : users) {
            assertThat(user.has("classroomId")).isFalse();
            assertThat(user.has("schoolId")).isTrue();
        }

        assertThat(schemaCount("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_NAME) = 'classrooms'")).isZero();
        assertThat(schemaCount("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE LOWER(TABLE_NAME) = 'users' AND LOWER(COLUMN_NAME) = 'classroom_id'")).isZero();
    }

    @Test
    void checkedInContractMatchesEveryHttpOperation() throws Exception {
        var response = send("GET", "/openapi.json", null);

        assertThat(response.statusCode()).isEqualTo(200);

        var contract = json.readTree(response.body());

        assertThat(contract.path("openapi").asString()).isEqualTo("3.2.1");

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
                .header("Content-Type", "text/plain").header("Authorization", "Bearer " + token).POST(HttpRequest.BodyPublishers.ofString("invalid")).build();

        assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(415);
    }

    @Test
    void createsUsersWithUnicodePasswords() throws Exception {
        var request = json.createObjectNode();

        request.put("name", "Test User");
        request.put("email", "test@example.test");
        request.put("password", "parolă-test");
        request.put("phone", "0712345678");
        request.put("schoolId", SCHOOL_A.toString());
        request.put("role", "USER");

        var response = send("POST", "/api/users", json.writeValueAsString(request));

        assertThat(response.statusCode()).isEqualTo(201);

        String hash = userRepository.findByEmail("test@example.test").orElseThrow().getPasswordHash();

        assertThat(passwords.matches("parolă-test", hash)).isTrue();
    }

    @Test
    void feedbackAndNotificationFeaturesAreRemoved() throws Exception {
        for (String endpoint : List.of("/api/feedbacks", "/api/notifications")) {
            assertThat(send("GET", endpoint, null).statusCode()).isEqualTo(404);
            assertThat(send("GET", endpoint + "/" + UUID.randomUUID(), null).statusCode()).isEqualTo(404);
        }

        String contract = send("GET", "/openapi.json", null).body();

        assertThat(contract).doesNotContain("/api/feedbacks", "/api/notifications", "FeedbackDto", "FeedbackType", "NotificationDto");
        assertThat(schemaCount("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_NAME) IN ('feedbacks', 'notifications')")).isZero();
        assertThat(json.readTree(send("GET", "/api/users", null).body()).size()).isEqualTo(2);
    }

    @Test
    void letterSignCrudIsNoLongerExposed() throws Exception {
        assertThat(send("GET", "/api/letter-sign-pairs", null).statusCode()).isEqualTo(404);

        var contract = json.readTree(send("GET", "/openapi.json", null).body());

        assertThat(contract.path("paths").has("/api/letter-sign-pairs")).isFalse();
        assertThat(contract.path("components").path("schemas").has("LetterSignPairDto")).isFalse();
    }

    @Test
    void categoryCreationIsUnavailableEvenForAdministrators() throws Exception {
        Long before = categoryRepository.count();
        var request = json.createObjectNode().put("name", "Categorie nouă").put("imageUrl", "https://open-hands-learning.s3.us-east-1.amazonaws.com/categories/alfabet/new_category.webp");

        assertThat(send("POST", "/api/categories", json.writeValueAsString(request)).statusCode()).isEqualTo(405);

        request.put("parentId", CATEGORY_PARENT.toString());

        assertThat(send("POST", "/api/categories", json.writeValueAsString(request)).statusCode()).isEqualTo(405);
        assertThat(categoryRepository.count()).isEqualTo(before);

        var contract = json.readTree(send("GET", "/openapi.json", null).body());

        assertThat(contract.path("paths").path("/api/categories").has("post")).isFalse();
        assertThat(send("GET", "/api/categories", null).statusCode()).isEqualTo(200);
    }

    @Test
    void categoryPagesKeepParentsAndChildrenSeparateAndAlphabetical() throws Exception {
        updateCategory(CATEGORY_PARENT, category -> category.setName("Zebră"));
        updateCategory(CATEGORY_CHILD, category -> category.setName("Șarpe"));

        var request = json.createObjectNode().put("name", "Arici")
                .put("imageUrl", "https://open-hands-learning.s3.us-east-1.amazonaws.com/categories/alfabet/arici.webp");

        String id = createCategory("Arici",
                "https://open-hands-learning.s3.us-east-1.amazonaws.com/categories/alfabet/arici.webp").toString();

        var roots = json.readTree(send("GET", "/api/categories/filter", null).body());

        assertThat(roots.size()).isEqualTo(2);
        assertThat(roots.get(0).has("code")).isFalse();
        assertThat(roots.get(0).has("sectionId")).isFalse();
        assertThat(roots.get(0).has("isExpanded")).isFalse();
        assertThat(List.of(roots.get(0).path("name").asString(), roots.get(1).path("name").asString()))
                .containsExactly("Arici", "Zebră");
        assertThat(roots.get(0).path("parentId").isNull()).isTrue();
        assertThat(roots.get(1).path("parentId").isNull()).isTrue();

        var searchedRoots = json.readTree(send("GET", "/api/categories/filter?name=%20aR%20", null).body());

        assertThat(searchedRoots.size()).isEqualTo(1);
        assertThat(searchedRoots.get(0).path("name").asString()).isEqualTo("Arici");
        assertThat(json.readTree(send("GET", "/api/categories/filter?name=ici", null).body()).size()).isZero();
        assertThat(json.readTree(send("GET", "/api/categories/filter?name=%20%20", null).body()).size()).isEqualTo(2);

        request.put("name", "Țară").put("parentId", CATEGORY_PARENT.toString());

        assertThat(send("PUT", "/api/categories/" + id, json.writeValueAsString(request)).statusCode()).isEqualTo(200);

        var children = json.readTree(send("GET", "/api/categories/filter?parentId=" + CATEGORY_PARENT, null).body());

        assertThat(children.size()).isEqualTo(2);
        assertThat(List.of(children.get(0).path("name").asString(), children.get(1).path("name").asString()))
                .containsExactly("Șarpe", "Țară");
        assertThat(children.get(0).path("parentId").asString()).isEqualTo(CATEGORY_PARENT.toString());
        assertThat(children.get(1).path("parentId").asString()).isEqualTo(CATEGORY_PARENT.toString());

        var searchedChildren = json.readTree(send("GET",
                "/api/categories/filter?parentId=" + CATEGORY_PARENT + "&name=%C8%99A", null).body());

        assertThat(searchedChildren.size()).isEqualTo(1);
        assertThat(searchedChildren.get(0).path("name").asString()).isEqualTo("Șarpe");
        assertThat(json.readTree(send("GET", "/api/categories/filter?parentId=" + CATEGORY_PARENT + "&name=pe", null).body()).size()).isZero();
        assertThat(json.readTree(send("GET", "/api/categories/filter?parentId=" + CATEGORY_CHILD + "&name=%C8%99", null).body()).size()).isZero();

        authenticate("ana@example.test");
        UUID ana = TestIds.USER_ANA;

        assertThat(send("PATCH", "/api/categories/favorites/save?userId=" + ana + "&categoryId=" + id, null)
                .statusCode()).isEqualTo(200);

        var favorites = json.readTree(send("GET", "/api/categories/favorites?userId=" + ana, null).body());

        assertThat(favorites.size()).isEqualTo(2);
        assertThat(List.of(favorites.get(0).path("name").asString(), favorites.get(1).path("name").asString()))
                .containsExactly("Șarpe", "Țară");

        var filteredFavorites = json.readTree(send("GET", "/api/categories/favorites/filter?userId=" + ana + "&name=%20%C8%9Ba%20", null).body());

        assertThat(filteredFavorites.size()).isEqualTo(1);
        assertThat(filteredFavorites.get(0).path("name").asString()).isEqualTo("Țară");
        assertThat(json.readTree(send("GET", "/api/categories/favorites/filter?userId=" + ana + "&name=ar", null).body()).size()).isZero();
        assertThat(json.readTree(send("GET", "/api/categories/favorites/filter?userId=" + ana + "&name=", null).body()).size()).isEqualTo(2);
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30)).header("Content-Type", "application/json");

        if (token != null && !path.equals("/api/auth/login")) builder.header("Authorization", "Bearer " + token);

        var request = builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
