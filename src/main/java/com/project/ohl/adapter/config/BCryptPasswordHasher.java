package com.project.ohl.adapter.config;

import com.project.ohl.application.port.out.PasswordHasher;
import com.project.ohl.domain.exception.InvalidRequestException;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public final class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCryptVersion.$2B, 12);

    @Override
    public String hash(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidRequestException("Password must not exceed 72 UTF-8 bytes.");
        }
        return encoder.encode(password);
    }
}
