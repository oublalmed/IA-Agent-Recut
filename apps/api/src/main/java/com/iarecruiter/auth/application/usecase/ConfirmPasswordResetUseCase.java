package com.iarecruiter.auth.application.usecase;

import com.iarecruiter.auth.domain.port.PasswordResetTokenRepository;
import com.iarecruiter.auth.domain.port.RefreshTokenRepository;
import com.iarecruiter.auth.domain.port.UserRepository;
import com.iarecruiter.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmPasswordResetUseCase {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);
        UUID userId = tokenRepository.findUserIdByValidToken(tokenHash)
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        userRepository.findById(userId).ifPresent(user -> {
            var updated = user.withPasswordHash(passwordEncoder.encode(newPassword))
                    .withUpdatedAt(Instant.now());
            userRepository.save(updated);
            refreshTokenRepository.revokeAllByUserId(userId);
        });

        tokenRepository.markUsed(tokenHash);
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
