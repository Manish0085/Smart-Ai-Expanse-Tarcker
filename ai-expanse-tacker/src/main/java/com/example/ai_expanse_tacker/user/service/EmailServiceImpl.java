package com.example.ai_expanse_tacker.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final WebClient webClient;
    private final String fromEmail;

    public EmailServiceImpl(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${mail.from}") String fromEmail) {
        this.fromEmail = fromEmail;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void sendVerificationEmail(String email, String link) {
    
        Map<String, Object> body = Map.of(
                // Recipient
                "to", List.of(Map.of("email", email)),
    
                // Sender (must be VERIFIED in Brevo)
                "sender", Map.of(
                        "email", fromEmail,
                        "name", "SecureTracker Pro"
                ),
    
                // Subject
                "subject", "SecureTracker Pro – Verify Your Account",
    
                // ✅ Plain-text fallback (IMPORTANT for Gmail)
                "textContent",
                "Verify your SecureTracker Pro account.\n\n" +
                "Click the link below to activate your account:\n" +
                link + "\n\n" +
                "If you did not create this account, you can ignore this email.",
    
                // ✅ HTML version
                "htmlContent",
                "<h2>Verify your account</h2>" +
                "<p>Click the button below to activate your SecureTracker Pro account:</p>" +
                "<p>" +
                "<a href=\"" + link + "\" target=\"_blank\" " +
                "style=\"display:inline-block;padding:12px 18px;" +
                "background:#4f46e5;color:white;text-decoration:none;" +
                "border-radius:6px;font-weight:600;\">" +
                "Verify Email</a>" +
                "</p>" +
                "<p style=\"margin-top:20px;font-size:12px;color:#666;\">" +
                "If you did not create this account, you can safely ignore this email." +
                "</p>"
        );
    
        try {
            webClient.post()
                    .uri("/smtp/email")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
    
            System.out.println("✅ Verification email sent via Brevo API to " + email);
    
        } catch (Exception e) {
            System.err.println("❌ EMAIL API FAILED: " + e.getMessage());
    
            System.out.println("\n--- MANUAL VERIFICATION LINK ---");
            System.out.println(link);
            System.out.println("--------------------------------\n");
    
            throw new RuntimeException("Email API failed: " + e.getMessage());
        }
    }
}
