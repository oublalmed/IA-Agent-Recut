package com.iarecruiter.auth.infrastructure.persistence.adapter;

import com.iarecruiter.auth.domain.port.PasswordResetTokenRepository;
import com.iarecruiter.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.iarecruiter.auth.infrastructure.persistence.jpa.JpaPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final JpaPasswordResetTokenRepository jpa;

    @Override
    public void save(UUID userId, String tokenHash, Instant expiresAt) {
        jpa.save(PasswordResetTokenEntity.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build());
    }

    @Override
    public Optional<UUID> findUserIdByValidToken(String tokenHash) {
        return jpa.findValidToken(tokenHash, Instant.now())
                .map(PasswordResetTokenEntity::getUserId);
    }

    @Override
    public void markUsed(String tokenHash) {
        jpa.markUsed(tokenHash, Instant.now());
    }
}
