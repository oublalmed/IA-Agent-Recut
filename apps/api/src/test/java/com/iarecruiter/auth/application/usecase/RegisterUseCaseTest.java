package com.iarecruiter.auth.application.usecase;

import com.iarecruiter.auth.domain.model.User;
import com.iarecruiter.auth.domain.model.UserRole;
import com.iarecruiter.auth.domain.port.CompanyRepository;
import com.iarecruiter.auth.domain.port.EmailPort;
import com.iarecruiter.auth.domain.port.UserRepository;
import com.iarecruiter.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private EmailPort emailPort;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUseCase registerUseCase;

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(companyRepository.createCompany(anyString(), anyString())).thenReturn(COMPANY_ID);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return u.withId(USER_ID);
        });
    }

    @Test
    void execute_createsUserAndCompany() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        var command = new RegisterUseCase.RegisterCommand(
                "test@example.com", "password123", "John", "Doe", "Acme Corp");
        User result = registerUseCase.execute(command);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(result.getCompanyId()).isEqualTo(COMPANY_ID);
        verify(companyRepository).createDefaultSubscription(COMPANY_ID);
        verify(companyRepository).createDefaultRetentionPolicy(COMPANY_ID);
        verify(emailPort).sendWelcomeEmail(eq("test@example.com"), eq("John"));
    }

    @Test
    void execute_throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        var command = new RegisterUseCase.RegisterCommand(
                "taken@example.com", "password123", "Jane", "Doe", "Corp");

        assertThatThrownBy(() -> registerUseCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
        verify(companyRepository, never()).createCompany(anyString(), anyString());
    }

    @Test
    void execute_normalizesEmailToLowercase() {
        when(userRepository.existsByEmail("upper@example.com")).thenReturn(false);

        var command = new RegisterUseCase.RegisterCommand(
                "UPPER@EXAMPLE.COM", "password123", "Test", "User", "Corp");
        User result = registerUseCase.execute(command);

        assertThat(result.getEmail()).isEqualTo("upper@example.com");
    }
}
