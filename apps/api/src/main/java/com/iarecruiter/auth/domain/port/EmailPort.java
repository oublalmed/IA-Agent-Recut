package com.iarecruiter.auth.domain.port;

public interface EmailPort {
    void sendPasswordResetEmail(String to, String resetLink);
    void sendWelcomeEmail(String to, String firstName);
}
