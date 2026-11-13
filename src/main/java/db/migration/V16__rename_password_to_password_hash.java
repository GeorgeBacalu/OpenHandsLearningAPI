package db.migration;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;

public final class V16__rename_password_to_password_hash extends BaseJavaMigration {
    private static final Pattern BCRYPT = Pattern.compile(
            "\\$2[aby]\\$(0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}");
    private record CredentialUpdate(UUID id, String hash) {}

    @Override
    public void migrate(Context context) throws SQLException {
        var connection = context.getConnection();
        var encoder = new BCryptPasswordEncoder(BCryptVersion.$2B, 12);
        var updates = new ArrayList<CredentialUpdate>();
        try (var statement = connection.createStatement();
                var rows = statement.executeQuery("SELECT id, password FROM users ORDER BY id FOR UPDATE")) {
            while (rows.next()) {
                String password = rows.getString("password");
                if (password == null || password.isBlank())
                    throw new SQLException("A legacy credential is empty; migration was not applied.");
                if (BCRYPT.matcher(password).matches()) continue;
                if (password.startsWith("$2"))
                    throw new SQLException("An invalid BCrypt credential requires a password reset.");
                if (password.getBytes(StandardCharsets.UTF_8).length > 72)
                    throw new SQLException("Legacy password exceeds BCrypt's 72-byte limit.");
                String hash = encoder.encode(password);
                if (!encoder.matches(password, hash))
                    throw new SQLException("Legacy credential verification failed.");
                updates.add(new CredentialUpdate(rows.getObject("id", UUID.class), hash));
            }
        }
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE users RENAME COLUMN password TO password_hash");
        }
        try (var statement = connection.prepareStatement("UPDATE users SET password_hash = ? WHERE id = ?")) {
            for (var update : updates) {
                statement.setString(1, update.hash());
                statement.setObject(2, update.id());
                if (statement.executeUpdate() != 1)
                    throw new SQLException("A user changed during credential migration.");
            }
        }
        try (var statement = connection.createStatement();
                var rows = statement.executeQuery("SELECT password_hash FROM users")) {
            while (rows.next()) {
                String hash = rows.getString(1);
                if (hash == null || !BCRYPT.matcher(hash).matches())
                    throw new SQLException("A stored credential is not BCrypt; migration was not applied.");
            }
        }
    }
}
