package com.iarecruiter.auth.application.usecase;

import com.iarecruiter.auth.domain.model.User;
import com.iarecruiter.auth.domain.port.RefreshTokenRepository;
import com.iarecruiter.auth.domain.port.UserRepository;
import com.iarecruiter.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginResult execute(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().strip())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(
                user.getId(), user.getEmail(), user.getRole().name(), user.getCompanyId());

        String rawRefreshToken = generateSecureToken();
        String tokenHash = hashToken(rawRefreshToken);
        Instant expiresAt = Instant.now().plusMillis(refreshExpirationMs);
        refreshTokenRepository.save(user.getId(), tokenHash, expiresAt);

        return new LoginResult(accessToken, rawRefreshToken, user);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record LoginResult(String accessToken, String refreshToken, User user) {}
}
