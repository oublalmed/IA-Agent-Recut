package com.iarecruiter.candidate.domain.port;

import com.iarecruiter.candidate.domain.model.Candidate;
import java.util.Optional;
import java.util.UUID;

public interface CandidateRepository {
    Candidate save(Candidate candidate);
    Optional<Candidate> findById(UUID id);
    Optional<Candidate> findByCompanyIdAndEmail(UUID companyId, String email);
}
