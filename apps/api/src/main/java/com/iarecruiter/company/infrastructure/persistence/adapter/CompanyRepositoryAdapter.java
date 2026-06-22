package com.iarecruiter.company.infrastructure.persistence.adapter;

import com.iarecruiter.auth.infrastructure.persistence.entity.CompanyEntity;
import com.iarecruiter.company.domain.model.Company;
import com.iarecruiter.company.domain.port.CompanyRepository;
import com.iarecruiter.company.infrastructure.persistence.jpa.JpaCompanyFullRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("companyModuleRepository")
@RequiredArgsConstructor
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final JpaCompanyFullRepository jpa;

    @Override
    public Optional<Company> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Company save(Company company) {
        CompanyEntity entity = toEntity(company);
        return toDomain(jpa.save(entity));
    }

    private Company toDomain(CompanyEntity e) {
        return Company.builder()
                .id(e.getId())
                .name(e.getName())
                .slug(e.getSlug())
                .logoUrl(e.getLogoUrl())
                .website(e.getWebsite())
                .industry(e.getIndustry())
                .sizeRange(e.getSizeRange())
                .country(e.getCountry())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private CompanyEntity toEntity(Company c) {
        return CompanyEntity.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .logoUrl(c.getLogoUrl())
                .website(c.getWebsite())
                .industry(c.getIndustry())
                .sizeRange(c.getSizeRange())
                .country(c.getCountry())
                .build();
    }
}
