package com.iarecruiter.candidate.infrastructure.persistence.adapter;

import com.iarecruiter.candidate.domain.port.ConsentLogRepository;
import com.iarecruiter.candidate.infrastructure.persistence.entity.ConsentLogEntity;
import com.iarecruiter.candidate.infrastructure.persistence.jpa.JpaConsentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConsentLogRepositoryAdapter implements ConsentLogRepository {
    private final JpaConsentLogRepository jpa;

    @Override
    public void logConsent(UUID candidateId, UUID companyId, String consentType, boolean granted, String ipAddress) {
        jpa.save(ConsentLogEntity.builder()
                .candidateId(candidateId).companyId(companyId)
                .consentType(consentType).granted(granted).ipAddress(ipAddress).build());
    }
}
