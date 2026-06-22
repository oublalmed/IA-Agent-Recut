package com.iarecruiter.auth.infrastructure.persistence.adapter;

import com.iarecruiter.auth.domain.port.CompanyRepository;
import com.iarecruiter.auth.infrastructure.persistence.entity.CompanyEntity;
import com.iarecruiter.auth.infrastructure.persistence.jpa.JpaCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final JpaCompanyRepository jpa;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public UUID createCompany(String name, String slug) {
        CompanyEntity company = CompanyEntity.builder()
                .name(name)
                .slug(slug)
                .build();
        return jpa.save(company).getId();
    }

    @Override
    public void createDefaultSubscription(UUID companyId) {
        jdbcTemplate.update("""
            INSERT INTO subscriptions (company_id, plan_type, cv_quota, period_start, period_end)
            VALUES (?, 'FREE', 50, CURRENT_DATE, CURRENT_DATE + INTERVAL '1 month')
            ON CONFLICT (company_id) DO NOTHING
            """, companyId);
    }

    @Override
    public void createDefaultRetentionPolicy(UUID companyId) {
        jdbcTemplate.update("""
            INSERT INTO data_retention_policies (company_id, resume_retention_days, candidate_retention_days, audit_retention_days)
            VALUES (?, 365, 730, 1825)
            ON CONFLICT (company_id) DO NOTHING
            """, companyId);
    }
}
