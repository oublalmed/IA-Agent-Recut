package com.iarecruiter.ai.domain.port;

import com.iarecruiter.ai.domain.model.AiReport;
import java.util.Optional;
import java.util.UUID;

public interface AiReportRepository {
    AiReport save(AiReport report);
    Optional<AiReport> findByApplicationId(UUID applicationId);
}
