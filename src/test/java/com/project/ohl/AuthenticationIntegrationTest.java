package com.project.ohl;

import com.project.ohl.application.port.out.PasswordHasher;
import java.net.CookieManager;
import java.net.CookiePolicy;
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
import tools.jackson.databind.json.JsonMapper;

import static com.project.ohl.TestIds.CATEGORY_PARENT;
import static com.project.ohl.TestIds.SCHOOL_A;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.servlet.session.cookie.secure=false")
class AuthenticationIntegrationTest extends DatabaseIntegrationTest {
    private static final String PASSWORD = "correct-test-password";
    @LocalServerPort private int port;
    @Autowired private JdbcTemplate database;
    @Autowired private PasswordHasher passwords;
    private final JsonMapper json = JsonMapper.builder().build();
    private CookieManager cookies;
    private HttpClient client;

    @BeforeEach
    void credentials() {
        cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder().cookieHandler(cookies).build();
        database.update("UPDATE users SET password_hash = ?", passwords.hash(PASSWORD));
    }

    @Test
    void guestsCannotReadAccountsOrFavoritesOrClaimAnIdentity() throws Exception {
        assertThat(send("GET", "/api/sections", null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/categories/favorites?userId=" + TestIds.USER_ANA, null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/api/categories/" + CATEGORY_PARENT + "?userId=" + TestIds.USER_ANA, null).statusCode()).isEqualTo(401);
    }

    @Test
    void registrationHashesPasswordsAndAssignsOnlyTheNormalRole() throws Exception {
        var request = json.createObjectNode().put("name", "New Account").put("email", "NEW@example.test")
                .put("password", PASSWORD).put("phone", "0712345678").put("schoolId", SCHOOL_A.toString())
                .put("roleId", TestIds.ROLE_ADMIN.toString());
        var created = send("POST", "/api/auth/register", json.writeValueAsString(request));
        assertThat(created.statusCode()).isEqualTo(201);
        var account = json.readTree(created.body());
        assertThat(account.path("authority").asString()).isEqualTo("USER");
        assertThat(account.path("roleId").asString()).isEqualTo(TestIds.ROLE_USER.toString());
        assertThat(account.path("email").asString()).isEqualTo("new@example.test");
        assertThat(account.has("password") || account.has("passwordHash") || account.has("password_hash")).isFalse();
        String hash = database.queryForObject("SELECT password_hash FROM users WHERE email = 'new@example.test'", String.class);
        assertThat(hash).startsWith("$2b$12$");
        assertThat(passwords.matches(PASSWORD, hash)).isTrue();
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/api/users", null).statusCode()).isEqualTo(403);
        assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(409);
    }

    @Test
    void invalidSchoolAndPasswordsCreateNoRecords() throws Exception {
        var request = json.createObjectNode().put("name", "Invalid Account").put("email", "invalid@example.test")
                .put("password", "short").put("phone", "0712345678").put("schoolId", SCHOOL_A.toString());
        assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(400);
        request.put("password", "é".repeat(37));
        assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(400);
        request.put("password", PASSWORD).put("schoolId", "00000000-0000-4000-8000-000000000999");
        assertThat(send("POST", "/api/auth/register", json.writeValueAsString(request)).statusCode()).isEqualTo(400);
        assertThat(database.queryForObject("SELECT COUNT(*) FROM users", Long.class)).isEqualTo(2);
    }

    @Test
    void loginRotatesTheSessionAndRejectsIncorrectCredentialsWithoutDisclosingAccounts() throws Exception {
        var wrong = login("ana@example.test", "incorrect-password");
        var missing = login("unknown@example.test", "incorrect-password");
        assertThat(wrong.statusCode()).isEqualTo(401);
        assertThat(missing.statusCode()).isEqualTo(401);
        assertThat(json.readTree(wrong.body()).path("detail").asString())
                .isEqualTo(json.readTree(missing.body()).path("detail").asString());
        csrf();
        String before = sessionId();
        var authenticated = login("ANA@example.test", PASSWORD);
        assertThat(authenticated.statusCode()).isEqualTo(200);
        assertThat(sessionId()).isNotEqualTo(before);
        assertThat(authenticated.headers().allValues("Set-Cookie").toString()).containsIgnoringCase("HttpOnly");
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
    void csrfIsRequiredAndLogoutInvalidatesTheServerSession() throws Exception {
        assertThat(raw("POST", "/api/auth/login", "{}", null).statusCode()).isEqualTo(403);
        assertThat(login("ana@example.test", PASSWORD).statusCode()).isEqualTo(200);
        String stolenCookie = "JSESSIONID=" + sessionId();
        assertThat(raw("POST", "/api/auth/logout", null, null).statusCode()).isEqualTo(403);
        assertThat(send("POST", "/api/auth/logout", null).statusCode()).isEqualTo(204);
        assertThat(send("GET", "/api/auth/me", null).statusCode()).isEqualTo(401);
        var request = HttpRequest.newBuilder(uri("/api/auth/me")).header("Cookie", stolenCookie).GET().build();
        assertThat(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
    }

    @Test
    void repeatedFailedLoginsAreThrottled() throws Exception {
        for (int i = 0; i < 5; i++) assertThat(login("throttle@example.test", "incorrect-password").statusCode()).isEqualTo(401);
        assertThat(login("throttle@example.test", "incorrect-password").statusCode()).isEqualTo(429);
    }

    private String sessionId() {
        return cookies.getCookieStore().getCookies().stream().filter(cookie -> cookie.getName().equals("JSESSIONID"))
                .findFirst().orElseThrow().getValue();
    }
    private HttpResponse<String> login(String email, String password) throws Exception {
        return send("POST", "/api/auth/login", json.writeValueAsString(json.createObjectNode().put("email", email).put("password", password)));
    }
    private String csrf() throws Exception { return json.readTree(raw("GET", "/api/auth/csrf", null, null).body()).path("token").asString(); }
    private URI uri(String path) { return URI.create("http://localhost:" + port + path); }
    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        return raw(method, path, body, method.equals("GET") ? null : csrf());
    }
    private HttpResponse<String> raw(String method, String path, String body, String token) throws Exception {
        var request = HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(30)).header("Content-Type", "application/json");
        if (token != null) request.header("X-CSRF-TOKEN", token);
        return client.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
