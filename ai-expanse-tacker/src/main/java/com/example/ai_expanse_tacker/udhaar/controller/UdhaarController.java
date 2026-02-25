package com.example.ai_expanse_tacker.udhaar.controller;

import com.example.ai_expanse_tacker.common.utils.SecurityUtils;
import com.example.ai_expanse_tacker.udhaar.entity.Udhaar;
import com.example.ai_expanse_tacker.udhaar.service.UdhaarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/udhaars")
public class UdhaarController {

    private final UdhaarService udhaarService;

    public UdhaarController(UdhaarService udhaarService) {
        this.udhaarService = udhaarService;
    }

    @GetMapping
    public List<Udhaar> getAllUdhaars() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return udhaarService.getUdhaarsByUser(userId);
    }
}
