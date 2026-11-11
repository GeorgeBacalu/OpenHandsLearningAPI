package com.project.ohl.adapter.config;

import com.project.ohl.application.port.out.PasswordHasher;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public final class Argon2PasswordHasher implements PasswordHasher {
    private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 19456, 2);

    @Override
    public String hash(String password) {
        return encoder.encode(password);
    }
}
