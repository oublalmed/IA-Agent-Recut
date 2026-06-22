package com.iarecruiter.auth.infrastructure.persistence.adapter;

import com.iarecruiter.auth.domain.port.RefreshTokenRepository;
import com.iarecruiter.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import com.iarecruiter.auth.infrastructure.persistence.jpa.JpaRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpa;

    @Override
    public void save(UUID userId, String tokenHash, Instant expiresAt) {
        jpa.save(RefreshTokenEntity.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build());
    }

    @Override
    public Optional<UUID> findUserIdByTokenHash(String tokenHash) {
        return jpa.findByTokenHashAndRevokedFalse(tokenHash)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(RefreshTokenEntity::getUserId);
    }

    @Override
    public void revokeByTokenHash(String tokenHash) {
        jpa.revokeByTokenHash(tokenHash);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        jpa.revokeAllByUserId(userId);
    }
}
