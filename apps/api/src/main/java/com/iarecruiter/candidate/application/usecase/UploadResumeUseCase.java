package com.iarecruiter.candidate.application.usecase;

import com.iarecruiter.candidate.domain.model.Candidate;
import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.domain.model.ResumeStatus;
import com.iarecruiter.candidate.domain.port.CandidateRepository;
import com.iarecruiter.candidate.domain.port.ConsentLogRepository;
import com.iarecruiter.candidate.domain.port.ResumeRepository;
import com.iarecruiter.candidate.domain.port.ResumeStoragePort;
import com.iarecruiter.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadResumeUseCase {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeStoragePort resumeStoragePort;
    private final ConsentLogRepository consentLogRepository;

    @Transactional
    public Resume execute(UploadCommand command) throws IOException {
        if (!ALLOWED_TYPES.contains(command.file().getContentType())) {
            throw new BusinessException("Invalid file type. Allowed: PDF, DOCX");
        }
        if (command.file().getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("File too large. Maximum size is 10MB");
        }

        // Find or create candidate
        Candidate candidate = candidateRepository
                .findByCompanyIdAndEmail(command.companyId(), command.candidateEmail())
                .orElseGet(() -> candidateRepository.save(Candidate.builder()
                        .id(UUID.randomUUID())
                        .companyId(command.companyId())
                        .email(command.candidateEmail().toLowerCase().strip())
                        .firstName(command.candidateFirstName())
                        .lastName(command.candidateLastName())
                        .erased(false)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()));

        // Log GDPR consent
        consentLogRepository.logConsent(candidate.getId(), command.companyId(),
                "CV_PROCESSING", true, command.ipAddress());

        // Upload to MinIO
        String objectKey = "resumes/" + command.companyId() + "/" + candidate.getId() + "/" + UUID.randomUUID() + getExtension(command.file().getContentType());
        resumeStoragePort.upload(objectKey, command.file().getInputStream(),
                command.file().getSize(), command.file().getContentType());

        Resume resume = Resume.builder()
                .id(UUID.randomUUID())
                .companyId(command.companyId())
                .candidateId(candidate.getId())
                .uploadedBy(command.uploadedBy())
                .originalFilename(command.file().getOriginalFilename())
                .minioObjectKey(objectKey)
                .mimeType(command.file().getContentType())
                .fileSizeBytes(command.file().getSize())
                .status(ResumeStatus.UPLOADED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return resumeRepository.save(resume);
    }

    private String getExtension(String contentType) {
        if (contentType == null) return "";
        return switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "application/msword" -> ".doc";
            default -> "";
        };
    }

    public record UploadCommand(
            UUID companyId,
            UUID uploadedBy,
            String candidateEmail,
            String candidateFirstName,
            String candidateLastName,
            MultipartFile file,
            String ipAddress
    ) {}
}
