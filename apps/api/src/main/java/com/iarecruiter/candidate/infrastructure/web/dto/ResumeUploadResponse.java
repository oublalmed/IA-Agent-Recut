package com.iarecruiter.candidate.infrastructure.web.dto;

import com.iarecruiter.candidate.domain.model.ResumeStatus;
import java.time.Instant;
import java.util.UUID;

public record ResumeUploadResponse(
        UUID id,
        UUID candidateId,
        String originalFilename,
        ResumeStatus status,
        Long fileSizeBytes,
        Instant createdAt
) {}
