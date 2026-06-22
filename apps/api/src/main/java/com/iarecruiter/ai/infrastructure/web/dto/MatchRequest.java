package com.iarecruiter.ai.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MatchRequest(@NotNull UUID jobId, @NotNull UUID resumeId) {}
