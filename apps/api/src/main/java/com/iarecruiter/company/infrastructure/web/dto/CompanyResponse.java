package com.iarecruiter.company.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl,
        String website,
        String industry,
        String sizeRange,
        String country,
        Instant createdAt,
        Instant updatedAt
) {}
