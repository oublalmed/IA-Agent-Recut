package com.iarecruiter.company.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@With
public class Company {
    private final UUID id;
    private final String name;
    private final String slug;
    private final String logoUrl;
    private final String website;
    private final String industry;
    private final String sizeRange;
    private final String country;
    private final Instant createdAt;
    private final Instant updatedAt;
}
