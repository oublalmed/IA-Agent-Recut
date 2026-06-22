package com.iarecruiter.company.application.usecase;

import com.iarecruiter.company.domain.model.Company;
import com.iarecruiter.company.domain.port.CompanyRepository;
import com.iarecruiter.company.domain.port.StoragePort;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadLogoUseCase {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "image/webp", "image/svg+xml");
    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024;

    private final CompanyRepository companyRepository;
    private final StoragePort storagePort;

    @Value("${minio.bucket-resumes}")
    private String bucket;

    @Transactional
    public Company execute(UUID companyId, MultipartFile file) throws IOException {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Invalid file type. Allowed: PNG, JPEG, WebP, SVG");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("Logo file must be under 2MB");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId.toString()));

        String objectKey = "logos/" + companyId + "/" + UUID.randomUUID() + getExtension(file.getContentType());
        String url = storagePort.uploadFile(bucket, objectKey, file.getInputStream(), file.getSize(), file.getContentType());

        Company updated = company.withLogoUrl(url).withUpdatedAt(Instant.now());
        return companyRepository.save(updated);
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> "";
        };
    }
}
