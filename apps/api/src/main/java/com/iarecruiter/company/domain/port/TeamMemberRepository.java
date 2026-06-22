package com.iarecruiter.company.domain.port;

import com.iarecruiter.company.domain.model.TeamMember;

import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository {
    List<TeamMember> findByCompanyId(UUID companyId);
    TeamMember invite(UUID companyId, String email, String firstName, String lastName, com.iarecruiter.auth.domain.model.UserRole role, String rawPassword);
}
