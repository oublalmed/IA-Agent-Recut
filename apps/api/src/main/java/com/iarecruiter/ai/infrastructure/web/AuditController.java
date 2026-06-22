package com.iarecruiter.ai.infrastructure.web;

import com.iarecruiter.ai.domain.model.ApplicationStatus;
import com.iarecruiter.ai.infrastructure.persistence.jpa.JpaAuditDecisionRepository;
import com.iarecruiter.ai.infrastructure.web.dto.AuditDecisionDetailResponse;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log endpoints")
public class AuditController {

    private final JpaAuditDecisionRepository jpaAuditDecisionRepository;

    @GetMapping("/decisions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get paginated audit decision log for the authenticated company")
    public ResponseEntity<Page<AuditDecisionDetailResponse>> getDecisions(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Object[]> raw = jpaAuditDecisionRepository.findDetailsByCompanyId(
                principal.getCompanyId(), pageable);

        List<AuditDecisionDetailResponse> content = raw.getContent().stream()
                .map(this::mapRow)
                .toList();

        return ResponseEntity.ok(new PageImpl<>(content, pageable, raw.getTotalElements()));
    }

    private AuditDecisionDetailResponse mapRow(Object[] row) {
        // row: id, application_id, candidate_email, candidate_name, job_title,
        //       decision, justification, ai_score, human_override, decided_at, decided_by
        return new AuditDecisionDetailResponse(
                toUuid(row[0]),
                toUuid(row[1]),
                str(row[2]),
                str(row[3]),
                str(row[4]),
                ApplicationStatus.valueOf(str(row[5])),
                str(row[6]),
                row[7] instanceof Number n ? n.doubleValue() : 0.0,
                row[8] instanceof Boolean b ? b : Boolean.parseBoolean(str(row[8])),
                row[9] instanceof Instant i ? i : Instant.parse(str(row[9])),
                toUuid(row[10])
        );
    }

    private UUID toUuid(Object o) {
        if (o == null) return null;
        if (o instanceof UUID u) return u;
        return UUID.fromString(o.toString());
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }
}
