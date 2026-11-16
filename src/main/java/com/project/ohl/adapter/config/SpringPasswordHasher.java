package com.project.ohl.adapter.config;

import com.project.ohl.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public final class SpringPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override public String hash(String password) {
        return encoder.encode(password);
    }

    @Override public boolean matches(String password, String hash) {
        return encoder.matches(password, hash);
    }
}
