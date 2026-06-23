package com.iarecruiter.ai.infrastructure.web;

import com.iarecruiter.ai.application.usecase.*;
import com.iarecruiter.ai.domain.model.*;
import com.iarecruiter.ai.infrastructure.web.dto.*;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI matching and ranking endpoints")
public class AiController {

    private final MatchCandidateUseCase matchCandidateUseCase;
    private final GetJobRankingUseCase getJobRankingUseCase;
    private final RecordAuditDecisionUseCase recordAuditDecisionUseCase;

    @PostMapping("/ai/match")
    @Operation(summary = "Match a resume against a job and generate AI score")
    public ResponseEntity<AiReportResponse> match(
            @Valid @RequestBody MatchRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        AiReport report = matchCandidateUseCase.execute(
                request.jobId(), request.resumeId(), principal.getCompanyId(), principal.getEmail());
        return ResponseEntity.ok(toReportResponse(report));
    }

    @GetMapping("/jobs/{jobId}/ranking")
    @Operation(summary = "Get ranked candidates for a job")
    public ResponseEntity<List<CandidateRankingResponse>> getRanking(
            @PathVariable UUID jobId,
            @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) List<String> skills,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        List<CandidateRanking> ranking = getJobRankingUseCase.execute(jobId, minScore, skills);
        return ResponseEntity.ok(ranking.stream().map(this::toRankingResponse).toList());
    }

    @GetMapping("/jobs/{jobId}/ranking/export")
    @Operation(summary = "Export ranked candidates for a job as CSV")
    public ResponseEntity<byte[]> exportRanking(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        List<CandidateRanking> ranking = getJobRankingUseCase.execute(jobId, null, null);

        StringBuilder csv = new StringBuilder();
        csv.append("rank,candidateName,candidateEmail,matchScore,recommendation\n");
        for (int i = 0; i < ranking.size(); i++) {
            CandidateRanking r = ranking.get(i);
            String name = ((r.getCandidateFirstName() != null ? r.getCandidateFirstName() : "") +
                    " " + (r.getCandidateLastName() != null ? r.getCandidateLastName() : "")).trim();
            csv.append(i + 1).append(",")
               .append(escapeCsv(name)).append(",")
               .append(escapeCsv(r.getCandidateEmail())).append(",")
               .append((int) Math.round(r.getMatchScore())).append(",")
               .append(escapeCsv(r.getRecommendation())).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "ranking-" + jobId + ".csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @PostMapping("/applications/{applicationId}/decision")
    @Operation(summary = "Record recruiter decision (human override of AI score)")
    public ResponseEntity<Void> recordDecision(
            @PathVariable UUID applicationId,
            @Valid @RequestBody AuditDecisionRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        recordAuditDecisionUseCase.execute(
                applicationId, principal.getId(), principal.getCompanyId(),
                request.finalStatus(), request.humanOverride(), request.justification());
        return ResponseEntity.noContent().build();
    }

    private AiReportResponse toReportResponse(AiReport r) {
        return new AiReportResponse(r.getId(), r.getApplicationId(), r.getMatchScore(),
                r.getSkillScore(), r.getExperienceScore(), r.getEducationScore(), r.getLanguageScore(),
                r.getStrengths(), r.getWeaknesses(), r.getRecommendation(),
                r.getModelUsed(), r.getGeneratedAt());
    }

    private CandidateRankingResponse toRankingResponse(CandidateRanking r) {
        return new CandidateRankingResponse(r.getApplicationId(), r.getCandidateId(),
                r.getCandidateEmail(), r.getCandidateFirstName(), r.getCandidateLastName(),
                r.getMatchScore(), r.getSkillScore(), r.getExperienceScore(),
                r.getStrengths(), r.getWeaknesses(), r.getRecommendation(),
                r.getStatus(), r.isHumanOverride(), r.getCvUrl());
    }
}
