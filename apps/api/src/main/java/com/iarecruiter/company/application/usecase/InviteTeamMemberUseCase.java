package com.iarecruiter.company.application.usecase;

import com.iarecruiter.auth.domain.model.UserRole;
import com.iarecruiter.company.domain.model.TeamMember;
import com.iarecruiter.company.domain.port.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteTeamMemberUseCase {

    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public TeamMember execute(UUID companyId, InviteCommand command) {
        return teamMemberRepository.invite(
                companyId,
                command.email(),
                command.firstName(),
                command.lastName(),
                command.role(),
                command.temporaryPassword()
        );
    }

    public record InviteCommand(
            String email,
            String firstName,
            String lastName,
            UserRole role,
            String temporaryPassword
    ) {}
}
