package com.example.ai_expanse_tacker.user.service;

import com.example.ai_expanse_tacker.user.entity.AppUser;
import com.example.ai_expanse_tacker.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final String baseUrl;

    public AuthService(
            UserRepository userRepository,
            EmailService emailService,
            @Value("${app.base-url}") String baseUrl) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.baseUrl = baseUrl;
    }

    public AppUser register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(password); // Note: In production, hash this!
        user.setVerified(false);
        AppUser savedUser = userRepository.save(user);

        // Verification Link
        String verificationLink = baseUrl + "/auth/verify?id=" + savedUser.getId();

        try {
            // Attempt to send real email
            emailService.sendVerificationEmail(email, verificationLink);
        } catch (Exception e) {
            // Log to console if email fails but KEEP the user saved
            System.err.println("CRITICAL: Registration succeeded but email failed to send.");
            System.err.println("MANUAL VERIFICATION LINK: " + verificationLink);

            // Re-throw if you want to notify the user, or just proceed
            throw new RuntimeException("User registered, but verification email failed: " + e.getMessage()
                    + ". Check server console for manual activation link!");
        }

        return savedUser;
    }

    public AppUser login(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email first. If email didn't arrive, check server logs.");
        }

        return user;
    }

    public String verifyEmail(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Invalid verification link"));

        user.setVerified(true);
        userRepository.save(user);
        return "<h1>Email Verified!</h1><p>Your account is now active. You can return to the dashboard and login.</p>";
    }
}
