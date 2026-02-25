package com.example.ai_expanse_tacker.user.controller;

import com.example.ai_expanse_tacker.user.entity.AppUser;
import com.example.ai_expanse_tacker.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            AppUser user = authService.register(request.get("email"), request.get("password"));
            return ResponseEntity
                    .ok(Map.of("message", "Registration successful. Please check your email for verification."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            AppUser user = authService.login(request.get("email"), request.get("password"));
            com.example.ai_expanse_tacker.common.utils.SecurityUtils.setCurrentUserId(user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        com.example.ai_expanse_tacker.common.utils.SecurityUtils.logout();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping(value = "/verify", produces = "text/html")
    public ResponseEntity<String> verify(@RequestParam UUID id) {
        try {
            String result = authService.verifyEmail(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("<h1>Verification Failed</h1><p>" + e.getMessage() + "</p>");
        }
    }
}
