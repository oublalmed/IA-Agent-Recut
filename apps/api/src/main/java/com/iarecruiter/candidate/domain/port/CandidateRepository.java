package com.iarecruiter.candidate.domain.port;

import com.iarecruiter.candidate.domain.model.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CandidateRepository {
    Candidate save(Candidate candidate);
    Optional<Candidate> findById(UUID id);
    Optional<Candidate> findByCompanyIdAndEmail(UUID companyId, String email);
    Page<Candidate> findByCompanyId(UUID companyId, Pageable pageable);
    Page<Candidate> search(UUID companyId, String query, Pageable pageable);
}
