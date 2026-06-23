package com.iarecruiter.candidate.infrastructure.web;

import com.iarecruiter.candidate.domain.model.Candidate;
import com.iarecruiter.candidate.domain.port.CandidateRepository;
import com.iarecruiter.candidate.infrastructure.persistence.jpa.JpaResumeRepository;
import com.iarecruiter.candidate.infrastructure.web.dto.CandidateListResponse;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidates", description = "Candidate management")
public class CandidateController {

    private final CandidateRepository candidateRepository;
    private final JpaResumeRepository jpaResumeRepository;

    @GetMapping
    @Operation(summary = "List candidates for the current company (paginated, with optional search)")
    public ResponseEntity<Page<CandidateListResponse>> listCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<CandidateListResponse> result = (search != null && !search.isBlank())
                ? candidateRepository.search(principal.getCompanyId(), search, pageRequest).map(this::toListResponse)
                : candidateRepository.findByCompanyId(principal.getCompanyId(), pageRequest).map(this::toListResponse);

        return ResponseEntity.ok(result);
    }

    private CandidateListResponse toListResponse(Candidate c) {
        long resumeCount = jpaResumeRepository.countByCandidateId(c.getId());
        return new CandidateListResponse(
                c.getId(),
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getPhone(),
                null, // currentTitle not stored on Candidate domain model
                c.getCreatedAt(),
                resumeCount
        );
    }
}
