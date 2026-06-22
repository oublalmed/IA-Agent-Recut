package com.iarecruiter.candidate.domain.port;

import com.iarecruiter.candidate.domain.model.Resume;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository {
    Resume save(Resume resume);
    Optional<Resume> findById(UUID id);
    void deleteById(UUID id);
    void deleteByCandidate(UUID candidateId);
    Optional<Resume> findLatestByCandidateId(UUID candidateId);
}
