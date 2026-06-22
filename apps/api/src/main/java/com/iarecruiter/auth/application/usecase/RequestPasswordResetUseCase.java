package com.iarecruiter.auth.application.usecase;

import com.iarecruiter.auth.domain.port.EmailPort;
import com.iarecruiter.auth.domain.port.PasswordResetTokenRepository;
import com.iarecruiter.auth.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailPort emailPort;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void execute(String email) {
        userRepository.findByEmail(email.toLowerCase().strip()).ifPresent(user -> {
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);
            Instant expiresAt = Instant.now().plusSeconds(3600);
            tokenRepository.save(user.getId(), tokenHash, expiresAt);
            String resetLink = frontendUrl + "/reset-password/confirm?token=" + rawToken;
            emailPort.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
