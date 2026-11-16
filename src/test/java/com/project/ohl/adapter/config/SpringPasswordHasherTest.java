package com.project.ohl.adapter.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

import static org.assertj.core.api.Assertions.assertThat;

class SpringPasswordHasherTest {
    private final SpringPasswordHasher hasher = new SpringPasswordHasher();

    @Test void newHashesUsePbkdf2AndFreshRandomSalts() {
        String password = "test password";
        var first = hasher.hash(password);
        var second = hasher.hash(password);
        assertThat(first).startsWith("{pbkdf2-sha256-600000}").isNotEqualTo(second).doesNotContain(password);
        var verifier = new Pbkdf2PasswordEncoder("", 16, 600000, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        assertThat(verifier.matches(password, first.substring(first.indexOf('}') + 1))).isTrue();
        assertThat(hasher.matches(password, second)).isTrue();
        assertThat(hasher.matches("incorrect", first)).isFalse();
    }

    @Test void shortUnicodeAndLongPasswordsAreAcceptedWithoutTruncation() {
        for (String password : new String[]{"a", "ș", "é".repeat(100), "a".repeat(2000)}) {
            String hash = hasher.hash(password);
            assertThat(hasher.matches(password, hash)).isTrue();
            assertThat(hasher.matches(password + "x", hash)).isFalse();
        }
    }

    @Test void existingBcryptCredentialsRemainValid() {
        String password = "existing-password";
        String legacy = new BCryptPasswordEncoder(4).encode(password);
        assertThat(hasher.matches(password, legacy)).isTrue();
        assertThat(hasher.matches(password + "x", legacy)).isFalse();
        assertThat(hasher.matches("a".repeat(100), legacy)).isFalse();
    }

    @Test void plaintextUnknownAndMalformedHashesCannotAuthenticate() {
        assertThat(hasher.matches("test", "test")).isFalse();
        assertThat(hasher.matches("test", "{unknown}hash")).isFalse();
        assertThat(hasher.matches("test", "{pbkdf2-sha256-600000}invalid")).isFalse();
        assertThat(hasher.matches(null, "hash")).isFalse();
        assertThat(hasher.matches("test", null)).isFalse();
    }
}
