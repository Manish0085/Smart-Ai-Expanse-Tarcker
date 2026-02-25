package com.example.ai_expanse_tacker.user.service;

import com.example.ai_expanse_tacker.ai.prompt.SystemPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String email, String link) {
        // Use SimpleMailMessage for maximum deliverability on unverified domains
        SimpleMailMessage message = new SimpleMailMessage();

        try {
            System.out.println(">>> Sending SimpleMailMessage to: " + email);
            System.out.println(">>> From: " + fromEmail);

            String body = SystemPrompt.VERIFICATION_EMAIL_BODY.replace("{{VERIFICATION_LINK}}", link);

            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("SecureTracker Pro - Verify Your Account");
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ SMTP Process Finished for " + email);
        } catch (Exception e) {
            System.err.println("❌ DELIVERY FAILED: " + e.getMessage());

            // Console box for manual override
            System.out.println("\n--- MANUAL VERIFICATION LINK (Copy & Paste) ---");
            System.out.println(link);
            System.out.println("-----------------------------------------------\n");

            throw new RuntimeException("Email service rejected by destination. Error: " + e.getMessage());
        }
    }
}
