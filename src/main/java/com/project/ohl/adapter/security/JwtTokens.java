package com.project.ohl.adapter.security;

import com.project.ohl.application.model.Account;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
public final class JwtTokens {
    private static final String ISSUER = "open-hands-learning";
    private static final Duration LIFETIME = Duration.ofMinutes(30);
    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Map<String, Instant> revoked = new HashMap<>();

    public JwtTokens() { this(randomKey()); }

    private static SecretKey randomKey() {
        // Restarting this single-instance API replaces the private key and signs everyone out.
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    JwtTokens(SecretKey key) {
        encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(ISSUER), token ->
                token.getAudience() != null && token.getAudience().contains("ohl-android") && token.getExpiresAt() != null
                        && token.getIssuedAt() != null && !token.getIssuedAt().isAfter(Instant.now())
                        && token.getSubject() != null && token.getId() != null && !isRevoked(token.getId())
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))));
    }

    public record AccessToken(String accessToken, String tokenType, long expiresIn, Account user) {
        @Override public String toString() { return "AccessToken[user=" + user.id() + "]"; }
    }

    public AccessToken issue(Account account) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(ISSUER).audience(List.of("ohl-android"))
                .subject(account.id().toString()).id(UUID.randomUUID().toString())
                .issuedAt(now).expiresAt(now.plus(LIFETIME)).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AccessToken(token, "Bearer", LIFETIME.toSeconds(), account);
    }

    public Jwt decode(String token) { return decoder.decode(token); }

    public synchronized void revoke(Jwt token) {
        prune();
        revoked.put(token.getId(), token.getExpiresAt());
    }

    private synchronized boolean isRevoked(String id) {
        prune();
        return revoked.containsKey(id);
    }

    private void prune() {
        var now = Instant.now();
        revoked.values().removeIf(expiration -> !expiration.isAfter(now));
    }
}
