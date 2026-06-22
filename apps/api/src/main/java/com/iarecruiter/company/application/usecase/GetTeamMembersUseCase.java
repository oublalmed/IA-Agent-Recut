package com.iarecruiter.company.application.usecase;

import com.iarecruiter.company.domain.model.TeamMember;
import com.iarecruiter.company.domain.port.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTeamMembersUseCase {

    private final TeamMemberRepository teamMemberRepository;

    public List<TeamMember> execute(UUID companyId) {
        return teamMemberRepository.findByCompanyId(companyId);
    }
}
