package com.iarecruiter.auth.domain.port;

import java.util.UUID;

public interface CompanyRepository {
    UUID createCompany(String name, String slug);
    void createDefaultSubscription(UUID companyId);
    void createDefaultRetentionPolicy(UUID companyId);
}
