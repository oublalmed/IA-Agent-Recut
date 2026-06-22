package com.iarecruiter.gdpr.infrastructure.web.dto;

import com.iarecruiter.gdpr.domain.model.DataRequestStatus;
import com.iarecruiter.gdpr.domain.model.DataRequestType;

import java.time.Instant;
import java.util.UUID;

public record DataRequestResponse(
        UUID id,
        UUID candidateId,
        UUID companyId,
        DataRequestType requestType,
        DataRequestStatus status,
        Instant requestedAt
) {}
