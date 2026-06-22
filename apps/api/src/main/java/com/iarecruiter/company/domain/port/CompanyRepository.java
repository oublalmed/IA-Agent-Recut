package com.iarecruiter.company.domain.port;

import com.iarecruiter.company.domain.model.Company;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {
    Optional<Company> findById(UUID id);
    Company save(Company company);
}
