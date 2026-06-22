package com.iarecruiter.candidate.domain.port;

import java.util.UUID;

public interface ConsentLogRepository {
    void logConsent(UUID candidateId, UUID companyId, String consentType, boolean granted, String ipAddress);
}
