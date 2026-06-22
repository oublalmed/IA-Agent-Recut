package com.iarecruiter.auth.infrastructure.persistence.adapter;

import com.iarecruiter.auth.domain.model.User;
import com.iarecruiter.auth.domain.port.UserRepository;
import com.iarecruiter.auth.infrastructure.persistence.entity.UserEntity;
import com.iarecruiter.auth.infrastructure.persistence.jpa.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    private UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.getId())
                .companyId(u.getCompanyId())
                .email(u.getEmail())
                .passwordHash(u.getPasswordHash())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .role(u.getRole())
                .isActive(u.isActive())
                .emailVerified(u.isEmailVerified())
                .lastLoginAt(u.getLastLoginAt())
                .build();
    }

    private User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .companyId(e.getCompanyId())
                .email(e.getEmail())
                .passwordHash(e.getPasswordHash())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .role(e.getRole())
                .active(e.isActive())
                .emailVerified(e.isEmailVerified())
                .lastLoginAt(e.getLastLoginAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
