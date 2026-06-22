package com.iarecruiter.candidate.infrastructure.persistence.jpa;

import com.iarecruiter.candidate.infrastructure.persistence.entity.CandidateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaCandidateRepository extends JpaRepository<CandidateEntity, UUID> {
    Optional<CandidateEntity> findByCompanyIdAndEmail(UUID companyId, String email);
    Page<CandidateEntity> findByCompanyId(UUID companyId, Pageable pageable);
}
