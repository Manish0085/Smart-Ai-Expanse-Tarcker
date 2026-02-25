package com.example.ai_expanse_tacker.user.service;

public interface EmailService {
    void sendVerificationEmail(String email, String link);
}
