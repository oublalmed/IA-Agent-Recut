package com.iarecruiter.company.application.usecase;

import com.iarecruiter.company.domain.model.Company;
import com.iarecruiter.company.domain.port.CompanyRepository;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCompanyUseCase {

    private final CompanyRepository companyRepository;

    public Company execute(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId.toString()));
    }
}
