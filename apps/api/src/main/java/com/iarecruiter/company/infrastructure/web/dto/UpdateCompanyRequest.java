package com.iarecruiter.company.infrastructure.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateCompanyRequest(
        @Size(max = 255) String name,
        @Size(max = 500) String website,
        @Size(max = 100) String industry,
        @Size(max = 50) String sizeRange,
        @Size(max = 100) String country
) {}
