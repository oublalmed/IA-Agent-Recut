package com.iarecruiter.ai.infrastructure.persistence.jpa;

import com.iarecruiter.ai.infrastructure.persistence.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {
    List<ApplicationEntity> findByJobId(UUID jobId);
    Optional<ApplicationEntity> findByJobIdAndCandidateId(UUID jobId, UUID candidateId);
}
