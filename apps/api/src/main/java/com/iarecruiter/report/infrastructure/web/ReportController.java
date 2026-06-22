package com.iarecruiter.report.infrastructure.web;

import com.iarecruiter.report.application.usecase.GetDashboardUseCase;
import com.iarecruiter.report.domain.model.DashboardKpis;
import com.iarecruiter.report.infrastructure.web.dto.DashboardResponse;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Dashboard and reporting endpoints")
public class ReportController {

    private final GetDashboardUseCase getDashboardUseCase;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard KPIs for the authenticated company")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECRUITER')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        DashboardKpis kpis = getDashboardUseCase.execute(principal.getCompanyId());
        return ResponseEntity.ok(new DashboardResponse(
                kpis.cvAnalyzed(),
                kpis.activeJobs(),
                kpis.avgMatchScore(),
                kpis.estimatedTimeSavedHours()
        ));
    }
}
