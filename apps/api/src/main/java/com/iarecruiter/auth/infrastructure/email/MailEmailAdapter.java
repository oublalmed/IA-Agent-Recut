package com.iarecruiter.auth.infrastructure.email;

import com.iarecruiter.auth.domain.port.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailEmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Reinitialisation de votre mot de passe — IA Recruiter");
            message.setText("Cliquez sur ce lien pour reinitialiser votre mot de passe (valable 1h) :\n\n" + resetLink);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Bienvenue sur IA Recruiter Agent !");
            message.setText("Bonjour " + firstName + ",\n\nVotre compte a ete cree avec succes. Bonne recrutement !\n\nL\'equipe IA Recruiter");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }
}
