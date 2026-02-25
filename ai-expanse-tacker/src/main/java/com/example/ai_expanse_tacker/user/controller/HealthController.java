package com.example.ai_expanse_tacker.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public @RestController public class HealthController {

    @GetMapping("/")
    public String home() {
        return "AI Expense Tracker API is running 🚀";
    }
}