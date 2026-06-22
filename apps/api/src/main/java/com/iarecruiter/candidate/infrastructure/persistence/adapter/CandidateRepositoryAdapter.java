package com.iarecruiter.candidate.infrastructure.persistence.adapter;

import com.iarecruiter.candidate.domain.model.Candidate;
import com.iarecruiter.candidate.domain.port.CandidateRepository;
import com.iarecruiter.candidate.infrastructure.persistence.entity.CandidateEntity;
import com.iarecruiter.candidate.infrastructure.persistence.jpa.JpaCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CandidateRepositoryAdapter implements CandidateRepository {
    private final JpaCandidateRepository jpa;

    @Override
    public Candidate save(Candidate c) {
        return toDomain(jpa.save(toEntity(c)));
    }

    @Override
    public Optional<Candidate> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Candidate> findByCompanyIdAndEmail(UUID companyId, String email) {
        return jpa.findByCompanyIdAndEmail(companyId, email).map(this::toDomain);
    }

    private Candidate toDomain(CandidateEntity e) {
        return Candidate.builder()
                .id(e.getId()).companyId(e.getCompanyId()).email(e.getEmail())
                .firstName(e.getFirstName()).lastName(e.getLastName())
                .phone(e.getPhone()).linkedinUrl(e.getLinkedinUrl()).location(e.getLocation())
                .erased(e.isErased()).erasedAt(e.getErasedAt())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }

    private CandidateEntity toEntity(Candidate c) {
        return CandidateEntity.builder()
                .id(c.getId()).companyId(c.getCompanyId()).email(c.getEmail())
                .firstName(c.getFirstName()).lastName(c.getLastName())
                .phone(c.getPhone()).linkedinUrl(c.getLinkedinUrl()).location(c.getLocation())
                .isErased(c.isErased()).erasedAt(c.getErasedAt()).build();
    }
}
