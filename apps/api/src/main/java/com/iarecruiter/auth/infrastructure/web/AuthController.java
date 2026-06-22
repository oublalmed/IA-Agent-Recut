package com.iarecruiter.auth.infrastructure.web;

import com.iarecruiter.auth.application.usecase.*;
import com.iarecruiter.auth.domain.model.User;
import com.iarecruiter.auth.infrastructure.web.dto.*;
import com.iarecruiter.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @PostMapping("/auth/register")
    @Operation(summary = "Register a new user and company")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUseCase.execute(new RegisterUseCase.RegisterCommand(
                request.email(), request.password(),
                request.firstName(), request.lastName(), request.companyName()
        ));
        LoginUseCase.LoginResult loginResult = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthResponse(loginResult));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Login and get JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUseCase.LoginResult result = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(toAuthResponse(result));
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Revoke refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.execute(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/reset-password")
    @Operation(summary = "Request password reset email")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        requestPasswordResetUseCase.execute(request.email());
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent"));
    }

    @PostMapping("/auth/reset-password/confirm")
    @Operation(summary = "Confirm password reset with token")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        confirmPasswordResetUseCase.execute(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = getCurrentUserUseCase.execute(principal.getId());
        return ResponseEntity.ok(new UserMeResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole(), user.getCompanyId(), user.isEmailVerified(), user.getCreatedAt()
        ));
    }

    private AuthResponse toAuthResponse(LoginUseCase.LoginResult result) {
        User user = result.user();
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                new AuthResponse.UserResponse(
                        user.getId(), user.getEmail(), user.getFirstName(),
                        user.getLastName(), user.getRole(), user.getCompanyId()
                )
        );
    }
}
