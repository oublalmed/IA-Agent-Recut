package com.iarecruiter.gdpr.infrastructure.web;

import com.iarecruiter.gdpr.application.usecase.SubmitDataRequestUseCase;
import com.iarecruiter.gdpr.domain.model.DataRequest;
import com.iarecruiter.gdpr.infrastructure.web.dto.DataRequestRequest;
import com.iarecruiter.gdpr.infrastructure.web.dto.DataRequestResponse;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "GDPR", description = "GDPR data request endpoints")
public class GdprController {

    private final SubmitDataRequestUseCase submitDataRequestUseCase;

    @PostMapping("/{candidateId}/data-request")
    @Operation(summary = "Submit a GDPR data request (ACCESS, ERASURE, or PORTABILITY)")
    public ResponseEntity<DataRequestResponse> submitDataRequest(
            @PathVariable UUID candidateId,
            @Valid @RequestBody DataRequestRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        DataRequest saved = submitDataRequestUseCase.execute(
                candidateId, principal.getCompanyId(), request.requestType());

        return ResponseEntity.status(HttpStatus.CREATED).body(new DataRequestResponse(
                saved.getId(), saved.getCandidateId(), saved.getCompanyId(),
                saved.getRequestType(), saved.getStatus(), saved.getRequestedAt()));
    }
}
