package com.iarecruiter.auth.application.usecase;

import com.iarecruiter.auth.domain.model.User;
import com.iarecruiter.auth.domain.model.UserRole;
import com.iarecruiter.auth.domain.port.CompanyRepository;
import com.iarecruiter.auth.domain.port.EmailPort;
import com.iarecruiter.auth.domain.port.UserRepository;
import com.iarecruiter.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final EmailPort emailPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User execute(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessException("Email already in use");
        }

        UUID companyId = companyRepository.createCompany(command.companyName(), toSlug(command.companyName()));
        companyRepository.createDefaultSubscription(companyId);
        companyRepository.createDefaultRetentionPolicy(companyId);

        User user = User.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .email(command.email().toLowerCase().strip())
                .passwordHash(passwordEncoder.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .role(UserRole.ADMIN)
                .active(true)
                .emailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User saved = userRepository.save(user);
        emailPort.sendWelcomeEmail(saved.getEmail(), saved.getFirstName());
        return saved;
    }

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = WHITESPACE.matcher(normalized.toLowerCase(Locale.ENGLISH)).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        return slug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public record RegisterCommand(
            String email,
            String password,
            String firstName,
            String lastName,
            String companyName
    ) {}
}
