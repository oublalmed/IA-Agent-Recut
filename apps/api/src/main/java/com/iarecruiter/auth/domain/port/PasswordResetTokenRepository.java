package com.iarecruiter.auth.domain.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {
    void save(UUID userId, String tokenHash, Instant expiresAt);
    Optional<UUID> findUserIdByValidToken(String tokenHash);
    void markUsed(String tokenHash);
}
