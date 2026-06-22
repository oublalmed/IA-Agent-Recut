package com.iarecruiter.company.infrastructure.persistence.adapter;

import com.iarecruiter.auth.domain.model.UserRole;
import com.iarecruiter.auth.infrastructure.persistence.entity.UserEntity;
import com.iarecruiter.auth.infrastructure.persistence.jpa.JpaUserRepository;
import com.iarecruiter.company.domain.model.TeamMember;
import com.iarecruiter.company.domain.port.TeamMemberRepository;
import com.iarecruiter.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TeamMemberRepositoryAdapter implements TeamMemberRepository {

    private final JpaUserRepository jpaUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<TeamMember> findByCompanyId(UUID companyId) {
        return jpaUserRepository.findAll().stream()
                .filter(u -> companyId.equals(u.getCompanyId()))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public TeamMember invite(UUID companyId, String email, String firstName, String lastName, UserRole role, String rawPassword) {
        if (jpaUserRepository.existsByEmail(email.toLowerCase())) {
            throw new BusinessException("Email already in use: " + email);
        }
        UserEntity entity = UserEntity.builder()
                .companyId(companyId)
                .email(email.toLowerCase().strip())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(true)
                .emailVerified(false)
                .build();
        return toDomain(jpaUserRepository.save(entity));
    }

    private TeamMember toDomain(UserEntity e) {
        return TeamMember.builder()
                .id(e.getId())
                .companyId(e.getCompanyId())
                .email(e.getEmail())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .role(e.getRole())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
