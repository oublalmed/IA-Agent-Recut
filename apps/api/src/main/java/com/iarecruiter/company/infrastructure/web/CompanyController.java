package com.iarecruiter.company.infrastructure.web;

import com.iarecruiter.company.application.usecase.*;
import com.iarecruiter.company.domain.model.Company;
import com.iarecruiter.company.domain.model.TeamMember;
import com.iarecruiter.company.infrastructure.web.dto.*;
import com.iarecruiter.shared.exception.BusinessException;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company management endpoints")
public class CompanyController {

    private final GetCompanyUseCase getCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final UploadLogoUseCase uploadLogoUseCase;
    private final GetTeamMembersUseCase getTeamMembersUseCase;
    private final InviteTeamMemberUseCase inviteTeamMemberUseCase;

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<CompanyResponse> getCompany(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertSameCompany(id, principal);
        Company company = getCompanyUseCase.execute(id);
        return ResponseEntity.ok(toResponse(company));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update company profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCompanyRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertSameCompany(id, principal);
        Company updated = updateCompanyUseCase.execute(id, new UpdateCompanyUseCase.UpdateCommand(
                request.name(), request.website(), request.industry(),
                request.sizeRange(), request.country(), null
        ));
        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload company logo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CompanyResponse> uploadLogo(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser principal) throws IOException {
        assertSameCompany(id, principal);
        Company updated = uploadLogoUseCase.execute(id, file);
        return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List team members")
    public ResponseEntity<List<TeamMemberResponse>> getMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertSameCompany(id, principal);
        List<TeamMember> members = getTeamMembersUseCase.execute(id);
        return ResponseEntity.ok(members.stream().map(this::toMemberResponse).toList());
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Invite a team member")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<TeamMemberResponse> inviteMember(
            @PathVariable UUID id,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        assertSameCompany(id, principal);
        TeamMember member = inviteTeamMemberUseCase.execute(id, new InviteTeamMemberUseCase.InviteCommand(
                request.email(), request.firstName(), request.lastName(),
                request.role(), request.temporaryPassword()
        ));
        return ResponseEntity.status(201).body(toMemberResponse(member));
    }

    private void assertSameCompany(UUID companyId, AuthenticatedUser principal) {
        if (!companyId.equals(principal.getCompanyId())) {
            throw new BusinessException("Access denied to this company");
        }
    }

    private CompanyResponse toResponse(Company c) {
        return new CompanyResponse(c.getId(), c.getName(), c.getSlug(), c.getLogoUrl(),
                c.getWebsite(), c.getIndustry(), c.getSizeRange(), c.getCountry(),
                c.getCreatedAt(), c.getUpdatedAt());
    }

    private TeamMemberResponse toMemberResponse(TeamMember m) {
        return new TeamMemberResponse(m.getId(), m.getEmail(), m.getFirstName(),
                m.getLastName(), m.getRole(), m.isActive(), m.getCreatedAt());
    }
}
