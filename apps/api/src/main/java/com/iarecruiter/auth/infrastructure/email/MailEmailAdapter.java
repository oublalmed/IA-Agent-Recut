package com.iarecruiter.auth.infrastructure.email;

import com.iarecruiter.auth.domain.port.EmailPort;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

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
            String template = new ClassPathResource("templates/password-reset-email.html")
                    .getContentAsString(StandardCharsets.UTF_8);
            String htmlBody = template.replace("{{resetUrl}}", resetLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe — IA Recruiter");
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendMatchingCompleteEmail(String toEmail, String candidateName, String jobTitle, int matchScore, String rankingUrl) {
        try {
            String scoreColor = matchScore >= 70 ? "#22c55e" : matchScore >= 40 ? "#f59e0b" : "#ef4444";
            String htmlBody = "<!DOCTYPE html><html><body style=\"font-family:sans-serif;max-width:600px;margin:0 auto;padding:20px\">" +
                    "<h2 style=\"color:#1a1a2e\">Matching IA terminé</h2>" +
                    "<p>Le matching pour <strong>" + candidateName + "</strong> sur le poste <strong>" + jobTitle + "</strong> est terminé.</p>" +
                    "<p>Score de matching : <span style=\"font-size:24px;font-weight:bold;color:" + scoreColor + "\">" + matchScore + "%</span></p>" +
                    "<a href=\"" + rankingUrl + "\" style=\"display:inline-block;background:#6366f1;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;margin:16px 0\">Voir le classement</a>" +
                    "<p style=\"color:#999;font-size:11px\">IA Recruiter Agent</p>" +
                    "</body></html>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Matching IA terminé — " + candidateName + " / " + jobTitle);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send matching complete email to {}: {}", toEmail, e.getMessage());
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
