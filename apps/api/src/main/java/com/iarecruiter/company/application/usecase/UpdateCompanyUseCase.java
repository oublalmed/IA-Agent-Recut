package com.iarecruiter.company.application.usecase;

import com.iarecruiter.company.domain.model.Company;
import com.iarecruiter.company.domain.port.CompanyRepository;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCompanyUseCase {

    private final CompanyRepository companyRepository;

    @Transactional
    public Company execute(UUID companyId, UpdateCommand command) {
        Company existing = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId.toString()));

        Company updated = existing
                .withName(command.name() != null ? command.name() : existing.getName())
                .withWebsite(command.website() != null ? command.website() : existing.getWebsite())
                .withIndustry(command.industry() != null ? command.industry() : existing.getIndustry())
                .withSizeRange(command.sizeRange() != null ? command.sizeRange() : existing.getSizeRange())
                .withCountry(command.country() != null ? command.country() : existing.getCountry())
                .withLogoUrl(command.logoUrl() != null ? command.logoUrl() : existing.getLogoUrl())
                .withUpdatedAt(Instant.now());

        return companyRepository.save(updated);
    }

    public record UpdateCommand(
            String name,
            String website,
            String industry,
            String sizeRange,
            String country,
            String logoUrl
    ) {}
}
