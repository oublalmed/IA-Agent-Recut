package com.iarecruiter.candidate.infrastructure.persistence.jpa;

import com.iarecruiter.candidate.infrastructure.persistence.entity.ConsentLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaConsentLogRepository extends JpaRepository<ConsentLogEntity, UUID> {
}
