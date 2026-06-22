package com.iarecruiter.auth.infrastructure.persistence.jpa;

import com.iarecruiter.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    @Query("SELECT p FROM PasswordResetTokenEntity p WHERE p.tokenHash = :tokenHash AND p.usedAt IS NULL AND p.expiresAt > :now")
    Optional<PasswordResetTokenEntity> findValidToken(String tokenHash, Instant now);

    @Modifying
    @Query("UPDATE PasswordResetTokenEntity p SET p.usedAt = :now WHERE p.tokenHash = :tokenHash")
    void markUsed(String tokenHash, Instant now);
}
