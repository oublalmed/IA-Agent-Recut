package com.iarecruiter.candidate.infrastructure.web.dto;

import com.iarecruiter.candidate.domain.model.ResumeStatus;
import java.time.Instant;
import java.util.UUID;

public record ResumeResponse(
        UUID id,
        UUID candidateId,
        UUID companyId,
        String originalFilename,
        String mimeType,
        Long fileSizeBytes,
        ResumeStatus status,
        String extractedData,
        String extractionError,
        Instant processedAt,
        Instant createdAt
) {}
