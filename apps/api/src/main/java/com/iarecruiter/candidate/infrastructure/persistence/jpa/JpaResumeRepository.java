package com.iarecruiter.candidate.infrastructure.persistence.jpa;

import com.iarecruiter.candidate.infrastructure.persistence.entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaResumeRepository extends JpaRepository<ResumeEntity, UUID> {
    void deleteByCandidateId(UUID candidateId);
    long countByCandidateId(UUID candidateId);
    List<ResumeEntity> findByCandidateIdOrderByCreatedAtDesc(UUID candidateId);
}
