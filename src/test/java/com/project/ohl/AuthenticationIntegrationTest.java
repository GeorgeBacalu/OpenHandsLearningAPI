package com.project.ohl;

import com.project.ohl.application.port.out.PasswordHasher;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.SCHOOL_A;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationIntegrationTest extends DatabaseIntegrationTest {
    private static final String PASSWORD = "correct-test-password";

    @LocalServerPort
    private int port;

    @Autowired private
    JdbcTemplate database;

    @Autowired
    private PasswordHasher passwords;

    private final JsonMapper json = JsonMapper.builder().build();
    private String token;
    private HttpClient client;

    @BeforeEach
    void credentials() {
        token = null;
        client = HttpClient.newHttpClient();
        database.update("UPDATE users SET password_hash = ?", passwords.hash(PASSWORD));
    }

    @Test
    void guestsCannotReadAccountsOrFavoritesOrClaimAnIdentity() throws Exception {
        assertThat(send("GET", "/api/categories", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/categories", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/categories/filter", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/counties", null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/schools/search?cityId=" + TestIds.id(101), null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/categories/favorites?userId=" + TestIds.USER_ANA, null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/categories/" + CATEGORY_PARENT + "?userId=" + TestIds.USER_ANA, null).statusCode()).isEqualTo(401);
    }

    @Test
    void registrationHashesPasswordsAndAssignsOnlyTheNormalRole() throws Exception {
        var request = json.createObjectNode().put("name", "New Account").put("email", "NEW@example.test")
                .put("password", PASSWORD).put("phone", "0712345678").put("schoolId", SCHOOL_A.toString())
                .put("role", "ADMIN");
        var created = send("POST", "/api/auth/register", json.writeValueAsString(request));

        assertThat(created.statusCode()).isEqualTo(201);

        var account = json.readTree(created.body()).path("user");
        token = json.readTree(created.body()).path("accessToken").asString();

        assertThat(account.path("role").asString()).isEqualTo("USER");
        assertThat(account.has("roleId") || account.has("authority")).isFalse();
        assertThat(account.path("email").asString()).isEqualTo("new@example.test");
        assertThat(account.has("password") || account.has("passwordHash") || account.has("password_hash")).isFalse();

        String hash = database.queryForObject("SELECT password_hash FROM users WHERE email = 'new@example.test'", String.class);

        assertThat(hash).startsWith("$2");
        assertThat(passwords.matches(PASSWORD, hash)).isTrue();
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(403);
        assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(409);
    }

    @Test
    void changingTheStoredRoleUpdatesExistingTokenPermissions() throws Exception {
        var loggedIn = login("ana@example.test", PASSWORD);
        token = json.readTree(loggedIn.body()).path("accessToken").asString();
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(403);

        database.update("UPDATE users SET role = 'ADMIN' WHERE id = ?", TestIds.USER_ANA);
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(200);

        database.update("UPDATE users SET role = 'USER' WHERE id = ?", TestIds.USER_ANA);
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(403);
    }

    @Test
    void missingPasswordAndInvalidSchoolCreateNoRecords() throws Exception {
        var request = json.createObjectNode().put("name", "Invalid Account").put("email", "invalid@example.test")
                .put("password", "").put("phone", "0712345678").put("schoolId", SCHOOL_A.toString());

        assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(400);

        request.put("password", PASSWORD).put("schoolId", "00000000-0000-4000-8000-000000000999");

        assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(400);
        assertThat(database.queryForObject("SELECT COUNT(*) FROM users", Long.class)).isEqualTo(2);
    }

    @Test
    void registrationAndLoginAcceptShortAndUnicodePasswords() throws Exception {
        String[] values = {"a", "ș", "parolă cu diacritice", "x".repeat(60)};

        for (int i = 0; i < values.length; i++) {
            String email = "password-policy-" + i + "@example.test";
            var request = json.createObjectNode().put("name", "New user").put("email", email)
                    .put("password", values[i]).put("phone", "0712345678").put("schoolId", SCHOOL_A.toString());

            assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(201);
            assertThat(login(email, values[i]).statusCode()).isEqualTo(200);
            assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(200);
            assertThat(login(email, values[i] + "x").statusCode()).isEqualTo(401);
        }
    }

    @Test
    void existingBcryptAccountsCanStillLogin() throws Exception {
        database.update("UPDATE users SET password_hash = ? WHERE id = ?",
                new BCryptPasswordEncoder(4).encode(PASSWORD), TestIds.USER_ANA);

        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/auth/me", null).body()).contains("Ana");
    }

    @Test
    void loginIssuesJwtAndRejectsIncorrectCredentialsWithoutDisclosingAccounts() throws Exception {
        var wrong = login("ana@example.test", "incorrect-password");
        var missing = login("unknown@example.test", "incorrect-password");

        assertThat(wrong.statusCode()).isEqualTo(401);
        assertThat(missing.statusCode()).isEqualTo(401);
        assertThat(json.readTree(wrong.body()).path("detail").asString())
                .isEqualTo(json.readTree(missing.body()).path("detail").asString());

        var authenticated = login("ANA@example.test", PASSWORD);

        assertThat(authenticated.statusCode()).isEqualTo(200);
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(json.readTree(authenticated.body()).path("tokenType").asString()).isEqualTo("Bearer");
        assertThat(json.readTree(authenticated.body()).path("expiresIn").asLong()).isEqualTo(1800);
        assertThat(authenticated.headers().allValues("Set-Cookie")).isEmpty();
        assertThat(send("GET", "/api/categories", null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/categories", null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/auth/me", null).body()).contains("ana@example.test").doesNotContain("password", "$2b$");
    }

    @Test
    void usersCannotReadOrChangeAnotherAccountsFavoritesOrPromoteThemselves() throws Exception {
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/categories/favorites?userId=" + TestIds.USER_ANA, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/categories/favorites?userId=" + TestIds.USER_DAN, null).statusCode()).isEqualTo(403);
        assertThat(send("PATCH", "/api/categories/favorites/save?userId=" + TestIds.USER_DAN + "&categoryId=" + CATEGORY_PARENT, null).statusCode()).isEqualTo(403);
        assertThat(send("GET", "/api/categories/" + CATEGORY_PARENT + "?userId=" + TestIds.USER_DAN, null).statusCode()).isEqualTo(403);
        assertThat(send("PUT", "/api/users/" + TestIds.USER_ANA, "{}").statusCode()).isEqualTo(403);
        assertThat(login("dan@example.test", PASSWORD).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(200);
    }

    @Test
    void logoutRevokesJwtAndCookiesCannotAuthenticate() throws Exception {
        assertThat(send("POST", "/api/auth/logout", null).statusCode()).isEqualTo(401);
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);

        String revoked = token;

        assertThat(send("POST", "/api/auth/logout", null).statusCode()).isEqualTo(204);
        assertThat(raw("GET", "/api/auth/me", null, revoked).statusCode()).isEqualTo(401);
        assertThat(raw("GET", "/api/categories", null, revoked).statusCode()).isEqualTo(401);

        token = null;
        var request = HttpRequest.newBuilder(uri("/api/auth/me")).header("Cookie", "JSESSIONID=" + revoked).GET().build();
        assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
    }

    @Test
    void malformedAndTamperedTokensCannotReadProtectedContent() throws Exception {
        assertThat(raw("GET", "/api/categories", null, "not-a-jwt").statusCode()).isEqualTo(401);
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);

        String[] parts = token.split("\\.");
        String changed = parts[0] + "." + parts[1] + "." + (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);

        assertThat(raw("GET", "/api/categories", null, changed).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/auth/me", null).body()).contains("Ana");

        database.update("UPDATE users SET name = 'Ana actualizată' WHERE id = ?", TestIds.USER_ANA);
        assertThat(send("GET", "/api/auth/me", null).body()).contains("Ana actualizată");
    }

    @Test
    void repeatedFailedLoginsAreThrottled() throws Exception {
        for (int i = 0; i < 5; i++)
            assertThat(login("throttle@example.test", "incorrect-password").statusCode()).isEqualTo(401);
        assertThat(login("throttle@example.test", "incorrect-password").statusCode()).isEqualTo(429);
    }

    @Test
    void profileIncludesTheAuthenticatedUsersSchoolCityAndCounty() throws Exception {
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);

        var profile = json.readTree(send("GET", "/api/auth/me", null).body());

        assertThat(profile.path("name").asString()).isEqualTo("Ana");
        assertThat(profile.path("phone").asString()).isEqualTo("0712345678");
        assertThat(profile.path("schoolName").asString()).isEqualTo("School A");
        assertThat(profile.path("cityName").asString()).isEqualTo("Bucuresti");
        assertThat(profile.path("countyName").asString()).isEqualTo("Bucuresti");
        assertThat(profile.has("password") || profile.has("passwordHash")).isFalse();
    }

    @Test
    void passwordChangeRequiresTheCurrentPasswordAndRevokesTheActiveSession() throws Exception {
        assertThat(send("PUT", "/api/auth/password", "{}").statusCode()).isEqualTo(401);
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);

        var change = json.createObjectNode().put("currentPassword", "wrong").put("newPassword", "parolă-nouă");

        assertThat(send("PUT", "/api/auth/password", json.writeValueAsString(change)).statusCode()).isEqualTo(400);
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(200);

        change.put("currentPassword", PASSWORD).put("newPassword", " ");

        assertThat(send("PUT", "/api/auth/password", json.writeValueAsString(change)).statusCode()).isEqualTo(400);

        change.put("newPassword", "parolă-nouă");

        assertThat(send("PUT", "/api/auth/password", json.writeValueAsString(change)).statusCode()).isEqualTo(204);
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(401);
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(401);
        assertThat(login("ana@example.test", "parolă-nouă").statusCode()).isEqualTo(200);
        assertThat(login("dan@example.test", PASSWORD).statusCode()).isEqualTo(200);

        String hash = database.queryForObject("SELECT password_hash FROM users WHERE id = ?", String.class, TestIds.USER_ANA);

        assertThat(passwords.matches("parolă-nouă", hash)).isTrue();
    }

    @Test
    void accountDeletionRequiresPasswordDeletesFavoritesAndInvalidatesAllItsSessions() throws Exception {
        assertThat(send("DELETE", "/api/auth/me", "{}").statusCode()).isEqualTo(401);
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);

        String firstSession = token;

        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);

        var deletion = json.createObjectNode().put("currentPassword", "wrong");

        assertThat(send("DELETE", "/api/auth/me", json.writeValueAsString(deletion)).statusCode()).isEqualTo(400);
        assertThat(database.queryForObject("SELECT COUNT(*) FROM users", Long.class)).isEqualTo(2);

        deletion.put("currentPassword", PASSWORD).put("userId", TestIds.USER_DAN.toString());

        assertThat(send("DELETE", "/api/auth/me", json.writeValueAsString(deletion)).statusCode()).isEqualTo(204);
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(401);
        assertThat(raw("GET", "/api/auth/me", null, firstSession).statusCode()).isEqualTo(401);
        assertThat(database.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Long.class, TestIds.USER_ANA)).isZero();
        assertThat(database.queryForObject("SELECT COUNT(*) FROM favorites WHERE user_id = ?", Long.class, TestIds.USER_ANA)).isZero();
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(401);
        assertThat(login("dan@example.test", PASSWORD).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/categories/favorites?userId=" + TestIds.USER_DAN, null).body()).contains(CATEGORY_PARENT.toString());
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        var response = raw("POST", "/api/auth/login", json.writeValueAsString(json.createObjectNode().put("email", email).put("password", password)), null);
        if (response.statusCode() == 200) token = json.readTree(response.body()).path("accessToken").asString();
        return response;
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        return raw(method, path, body, token);
    }
    private HttpResponse<String> raw(String method, String path, String body, String token) throws Exception {
        var request = HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(30)).header("Content-Type", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);

        return client.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
