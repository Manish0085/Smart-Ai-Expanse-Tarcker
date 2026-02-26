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
                "to", List.of(Map.of("email", email)),
                "sender", Map.of(
                        "email", fromEmail,
                        "name", "SecureTracker Pro"),
                "subject", "SecureTracker Pro - Verify Your Account",
                "htmlContent",
                "<h2>Verify your account</h2>" +
                        "<p>Click the link below to activate your account:</p>" +
                        "<a href=\"" + link + "\">Verify Email</a>");

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