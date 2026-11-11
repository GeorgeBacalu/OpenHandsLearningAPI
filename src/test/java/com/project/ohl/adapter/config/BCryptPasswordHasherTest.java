package com.project.ohl.adapter.config;

import com.project.ohl.domain.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BCryptPasswordHasherTest {
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();
    private final BCryptPasswordEncoder verifier = new BCryptPasswordEncoder();

    @Test
    void usesBcrypt2bWithCost12AndFreshRandomSalts() {
        String password = "long-test-password";
        var first = hasher.hash(password);
        var second = hasher.hash(password);
        assertThat(first).startsWith("$2b$12$").hasSize(60);
        assertThat(second).startsWith("$2b$12$").isNotEqualTo(first);
        assertThat(verifier.matches(password, first)).isTrue();
        assertThat(verifier.matches(password, second)).isTrue();
        assertThat(verifier.matches("wrong-password", first)).isFalse();
    }

    @Test
    void acceptsExactly72Utf8Bytes() {
        for (String password : new String[]{"a".repeat(72), "\u00e9".repeat(36)}) {
            assertThat(verifier.matches(password, hasher.hash(password))).isTrue();
        }
    }

    @Test
    void rejectsOversizedAsciiAndMultibytePasswords() {
        for (String password : new String[]{"a".repeat(73), "\u00e9".repeat(37)}) {
            assertThatThrownBy(() -> hasher.hash(password))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage("Password must not exceed 72 UTF-8 bytes.");
        }
    }
}
