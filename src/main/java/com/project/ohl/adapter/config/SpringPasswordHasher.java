package com.project.ohl.adapter.config;

import com.project.ohl.application.port.out.PasswordHasher;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.stereotype.Component;

@Component
public final class SpringPasswordHasher implements PasswordHasher {
    private static final String FORMAT = "pbkdf2-sha256-600000";
    private final PasswordEncoder encoder;

    public SpringPasswordHasher() {
        var current = new Pbkdf2PasswordEncoder("", 16, 600000, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        var delegating = new DelegatingPasswordEncoder(FORMAT, Map.of(FORMAT, current));
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        encoder = delegating;
    }

    @Override public String hash(String password) { return encoder.encode(password); }

    @Override public boolean matches(String password, String hash) {
        if (password == null || hash == null) return false;
        if (!hash.startsWith("{" + FORMAT + "}")) {
            // Existing BCrypt accounts retain their exact credentials without truncation.
            if (!hash.matches("\\$2[aby]\\$\\d{2}\\$.{53}")
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) return false;
        }
        try { return encoder.matches(password, hash); }
        catch (IllegalArgumentException exception) { return false; }
    }
}
