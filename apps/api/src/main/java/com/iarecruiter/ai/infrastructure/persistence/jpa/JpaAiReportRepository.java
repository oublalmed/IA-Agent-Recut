package com.iarecruiter.ai.infrastructure.persistence.jpa;

import com.iarecruiter.ai.infrastructure.persistence.entity.AiReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaAiReportRepository extends JpaRepository<AiReportEntity, UUID> {
    Optional<AiReportEntity> findByApplicationId(UUID applicationId);
}
