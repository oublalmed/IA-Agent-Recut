package com.iarecruiter.gdpr.infrastructure.web.dto;

import com.iarecruiter.gdpr.domain.model.DataRequestType;
import jakarta.validation.constraints.NotNull;

public record DataRequestRequest(
        @NotNull DataRequestType requestType
) {}
