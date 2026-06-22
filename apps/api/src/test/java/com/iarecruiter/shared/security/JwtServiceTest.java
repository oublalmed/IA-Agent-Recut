package com.iarecruiter.shared.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final UUID userId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secret = Base64.getEncoder().encodeToString(
                "test-secret-key-for-unit-testing-purposes-only-min-256bits-padding".getBytes()
        );
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 900000L);
    }

    @Test
    void generateAndParseToken_roundtrip() {
        String token = jwtService.generateToken(userId, "test@example.com", "RECRUITER", companyId);

        assertThat(token).isNotBlank();
        Claims claims = jwtService.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("RECRUITER");
        assertThat(claims.get("companyId", String.class)).isEqualTo(companyId.toString());
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtService.generateToken(userId, "test@example.com", "ADMIN", companyId);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtService.generateToken(userId, "test@example.com", "ADMIN", companyId);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        JwtService shortLivedService = new JwtService();
        String secret = Base64.getEncoder().encodeToString(
                "test-secret-key-for-unit-testing-purposes-only-min-256bits-padding".getBytes()
        );
        ReflectionTestUtils.setField(shortLivedService, "secret", secret);
        ReflectionTestUtils.setField(shortLivedService, "expirationMs", -1000L);
        String token = shortLivedService.generateToken(userId, "test@example.com", "RECRUITER", companyId);
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
