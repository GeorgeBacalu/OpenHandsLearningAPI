package com.project.ohl.adapter.security;

import com.project.ohl.application.model.Account;
import com.project.ohl.domain.model.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokensTest {
    private final SecretKeySpec key = new SecretKeySpec(new byte[32], "HmacSHA256");
    private final JwtTokens tokens = new JwtTokens(key);
    private final Account account = new Account(UUID.randomUUID(), "Ana", "ana@example.test", "0712345678",
            Role.USER, UUID.randomUUID());

    @Test void issuedTokensIdentifyTheAccountAndAreRevokedIndividually() {
        var first = tokens.issue(account);
        var second = tokens.issue(account);
        var jwt = tokens.decode(first.accessToken());
        assertThat(jwt.getSubject()).isEqualTo(account.id().toString());
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
        assertThat(jwt.getClaims()).doesNotContainKeys("password", "email", "authority");
        tokens.revoke(jwt);
        assertThatThrownBy(() -> tokens.decode(first.accessToken())).isInstanceOf(JwtException.class);
        assertThat(tokens.decode(second.accessToken()).getSubject()).isEqualTo(account.id().toString());
    }

    @Test void expiredFutureAndMissingExpirationTokensAreRejectedEvenWithAValidSignature() {
        var now = Instant.now();
        assertRejected(claims().issuedAt(now.minusSeconds(100)).expiresAt(now.minusSeconds(1)).build());
        assertRejected(claims().notBefore(now.plusSeconds(60)).build());
        assertRejected(claims().issuedAt(now.plusSeconds(60)).expiresAt(now.plusSeconds(120)).build());
        var noExpiry = JwtClaimsSet.builder().issuer("open-hands-learning").audience(List.of("ohl-android"))
                .subject(account.id().toString()).id(UUID.randomUUID().toString()).issuedAt(now).build();
        assertRejected(noExpiry);
    }

    @Test void issuerAudienceAndAlgorithmCannotBeSubstituted() {
        assertRejected(claims().issuer("other-app").build());
        assertRejected(claims().audience(List.of("other-client")).build());
        assertRejected(claims().audience(List.of()).build());
        var encoder = NimbusJwtEncoder.withSecretKey(new SecretKeySpec(new byte[64], "HmacSHA512"))
                .algorithm(MacAlgorithm.HS512).build();
        var jwt = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims().build()));
        assertThatThrownBy(() -> tokens.decode(jwt.getTokenValue())).isInstanceOf(JwtException.class);
    }

    @Test void anApiRestartInvalidatesPreviouslyIssuedTokens() {
        var firstInstance = new JwtTokens();
        var secondInstance = new JwtTokens();
        String jwt = firstInstance.issue(account).accessToken();
        assertThatThrownBy(() -> secondInstance.decode(jwt)).isInstanceOf(JwtException.class);
    }

    private JwtClaimsSet.Builder claims() {
        var now = Instant.now();
        return JwtClaimsSet.builder().issuer("open-hands-learning").audience(List.of("ohl-android"))
                .subject(account.id().toString()).id(UUID.randomUUID().toString()).issuedAt(now).expiresAt(now.plusSeconds(60));
    }

    private void assertRejected(JwtClaimsSet claims) {
        var encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        String jwt = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        assertThatThrownBy(() -> tokens.decode(jwt)).isInstanceOf(JwtException.class);
    }
}
