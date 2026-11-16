package com.project.ohl.adapter.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SpringPasswordHasherTest {
    private final SpringPasswordHasher hasher = new SpringPasswordHasher();

    @Test void newHashesUseBcryptAndFreshRandomSalts() {
        String password = "test password";
        var first = hasher.hash(password);
        var second = hasher.hash(password);
        assertThat(first).startsWith("$2").isNotEqualTo(second).doesNotContain(password);
        assertThat(new BCryptPasswordEncoder().matches(password, first)).isTrue();
        assertThat(hasher.matches(password, second)).isTrue();
        assertThat(hasher.matches("incorrect", first)).isFalse();
    }

    @Test void shortAndUnicodePasswordsUseTheEncoderDefaults() {
        for (String password : new String[]{"a", "ș", "parolă cu diacritice"}) {
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
    }

    @Test void plaintextUnknownAndMalformedHashesCannotAuthenticate() {
        assertThat(hasher.matches("test", "test")).isFalse();
        assertThat(hasher.matches("test", "{unknown}hash")).isFalse();
        assertThat(hasher.matches("test", "$2a$invalid")).isFalse();
    }
}
