package com.iarecruiter.auth.application.usecase;

import com.iarecruiter.auth.domain.model.User;
import com.iarecruiter.auth.domain.model.UserRole;
import com.iarecruiter.auth.domain.port.RefreshTokenRepository;
import com.iarecruiter.auth.domain.port.UserRepository;
import com.iarecruiter.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private User activeUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginUseCase, "refreshExpirationMs", 604800000L);
        activeUser = User.builder()
                .id(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("$2a$12$hashedpassword")
                .role(UserRole.RECRUITER)
                .active(true)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void execute_returnsTokensOnValidCredentials() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", activeUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("jwt_token");

        LoginUseCase.LoginResult result = loginUseCase.execute("user@example.com", "password123");

        assertThat(result.accessToken()).isEqualTo("jwt_token");
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.user()).isEqualTo(activeUser);
        verify(refreshTokenRepository).save(eq(activeUser.getId()), anyString(), any(Instant.class));
    }

    @Test
    void execute_throwsOnUnknownEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute("unknown@example.com", "pass"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void execute_throwsOnWrongPassword() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.execute("user@example.com", "wrongpass"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void execute_throwsOnInactiveAccount() {
        User inactiveUser = activeUser.withActive(false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> loginUseCase.execute("user@example.com", "password123"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("disabled");
    }
}
