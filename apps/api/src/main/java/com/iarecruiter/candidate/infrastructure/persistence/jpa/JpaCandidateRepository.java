package com.iarecruiter.candidate.infrastructure.persistence.jpa;

import com.iarecruiter.candidate.infrastructure.persistence.entity.CandidateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface JpaCandidateRepository extends JpaRepository<CandidateEntity, UUID> {
    Optional<CandidateEntity> findByCompanyIdAndEmail(UUID companyId, String email);
    Page<CandidateEntity> findByCompanyId(UUID companyId, Pageable pageable);

    @Query("SELECT c FROM CandidateEntity c WHERE c.companyId = :companyId " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<CandidateEntity> search(@Param("companyId") UUID companyId,
                                  @Param("q") String q,
                                  Pageable pageable);
}
