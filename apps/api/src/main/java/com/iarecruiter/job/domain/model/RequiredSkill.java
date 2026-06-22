package com.iarecruiter.job.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class RequiredSkill {
    private final UUID id;
    private final UUID skillId;
    private final String skillName;
    private final BigDecimal weight;
    private final boolean mandatory;
}
