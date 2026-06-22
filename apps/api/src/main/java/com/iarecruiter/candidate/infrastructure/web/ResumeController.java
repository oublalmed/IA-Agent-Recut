package com.iarecruiter.candidate.infrastructure.web;

import com.iarecruiter.candidate.application.usecase.*;
import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.infrastructure.messaging.ResumeExtractionProducer;
import com.iarecruiter.candidate.infrastructure.web.dto.*;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "CV upload and management")
public class ResumeController {

    private final UploadResumeUseCase uploadResumeUseCase;
    private final GetResumeUseCase getResumeUseCase;
    private final DeleteResumeUseCase deleteResumeUseCase;
    private final ResumeExtractionProducer extractionProducer;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a CV (PDF or DOCX)")
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("candidateEmail") String candidateEmail,
            @RequestParam(value = "candidateFirstName", required = false) String firstName,
            @RequestParam(value = "candidateLastName", required = false) String lastName,
            @AuthenticationPrincipal AuthenticatedUser principal,
            HttpServletRequest request) throws IOException {

        Resume resume = uploadResumeUseCase.execute(new UploadResumeUseCase.UploadCommand(
                principal.getCompanyId(), principal.getId(),
                candidateEmail, firstName, lastName, file,
                request.getRemoteAddr()
        ));

        // Trigger async extraction via RabbitMQ
        extractionProducer.sendExtractionRequest(resume.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResumeUploadResponse(
                resume.getId(), resume.getCandidateId(), resume.getOriginalFilename(),
                resume.getStatus(), resume.getFileSizeBytes(), resume.getCreatedAt()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resume metadata")
    public ResponseEntity<ResumeResponse> getResume(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        Resume resume = getResumeUseCase.execute(id);
        assertSameCompany(resume, principal);
        return ResponseEntity.ok(toResponse(resume));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume (GDPR erasure)")
    public ResponseEntity<Void> deleteResume(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        deleteResumeUseCase.execute(id, principal.getCompanyId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/extracted-data")
    @Operation(summary = "Get extracted structured data from a resume")
    public ResponseEntity<String> getExtractedData(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        Resume resume = getResumeUseCase.execute(id);
        assertSameCompany(resume, principal);
        if (resume.getExtractedData() == null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("{\"status\":\"PROCESSING\"}");
        }
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(resume.getExtractedData());
    }

    private void assertSameCompany(Resume resume, AuthenticatedUser principal) {
        if (!resume.getCompanyId().equals(principal.getCompanyId())) {
            throw new BusinessException("Access denied to this resume");
        }
    }

    private ResumeResponse toResponse(Resume r) {
        return new ResumeResponse(r.getId(), r.getCandidateId(), r.getCompanyId(),
                r.getOriginalFilename(), r.getMimeType(), r.getFileSizeBytes(),
                r.getStatus(), r.getExtractedData(), r.getExtractionError(),
                r.getProcessedAt(), r.getCreatedAt());
    }
}
