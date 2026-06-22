package com.iarecruiter.candidate.infrastructure.persistence.adapter;

import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.candidate.infrastructure.persistence.entity.ResumeEntity;
import com.iarecruiter.candidate.infrastructure.persistence.jpa.JpaResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResumeRepositoryAdapter implements ResumeRepository {
    private final JpaResumeRepository jpa;

    @Override
    public Resume save(Resume r) {
        return toDomain(jpa.save(toEntity(r)));
    }

    @Override
    public Optional<Resume> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public void deleteByCandidate(UUID candidateId) {
        jpa.deleteByCandidateId(candidateId);
    }

    @Override
    public Optional<Resume> findLatestByCandidateId(UUID candidateId) {
        List<ResumeEntity> resumes = jpa.findByCandidateIdOrderByCreatedAtDesc(candidateId);
        return resumes.isEmpty() ? Optional.empty() : Optional.of(toDomain(resumes.get(0)));
    }

    private Resume toDomain(ResumeEntity e) {
        return Resume.builder()
                .id(e.getId()).companyId(e.getCompanyId()).candidateId(e.getCandidateId())
                .uploadedBy(e.getUploadedBy()).originalFilename(e.getOriginalFilename())
                .minioObjectKey(e.getMinioObjectKey()).mimeType(e.getMimeType())
                .fileSizeBytes(e.getFileSizeBytes()).status(e.getStatus())
                .extractedData(e.getExtractedData()).extractionError(e.getExtractionError())
                .processedAt(e.getProcessedAt()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }

    private ResumeEntity toEntity(Resume r) {
        return ResumeEntity.builder()
                .id(r.getId()).companyId(r.getCompanyId()).candidateId(r.getCandidateId())
                .uploadedBy(r.getUploadedBy()).originalFilename(r.getOriginalFilename())
                .minioObjectKey(r.getMinioObjectKey()).mimeType(r.getMimeType())
                .fileSizeBytes(r.getFileSizeBytes()).status(r.getStatus())
                .extractedData(r.getExtractedData()).extractionError(r.getExtractionError())
                .processedAt(r.getProcessedAt()).build();
    }
}
