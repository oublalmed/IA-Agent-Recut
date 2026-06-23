package com.iarecruiter.auth.domain.port;

public interface EmailPort {
    void sendPasswordResetEmail(String to, String resetLink);
    void sendWelcomeEmail(String to, String firstName);
    void sendMatchingCompleteEmail(String toEmail, String candidateName, String jobTitle, int matchScore, String rankingUrl);
}
