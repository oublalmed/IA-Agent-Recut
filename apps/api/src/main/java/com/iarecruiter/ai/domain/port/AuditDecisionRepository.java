package com.iarecruiter.ai.domain.port;

import com.iarecruiter.ai.domain.model.AuditDecision;
import java.util.List;
import java.util.UUID;

public interface AuditDecisionRepository {
    AuditDecision save(AuditDecision decision);
    List<AuditDecision> findByJobId(UUID jobId);
    List<AuditDecision> findByCompanyId(UUID companyId);
}
