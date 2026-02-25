package com.example.ai_expanse_tacker.common.utils;

import java.util.UUID;

/**
 * Enhanced Security Utility to manage simulated login state.
 */
public class SecurityUtils {

    private static UUID currentUserId = null;

    public static UUID getCurrentUserId() {
        if (currentUserId == null) {
            // Default dev user if not logged in
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        return currentUserId;
    }

    public static void setCurrentUserId(UUID userId) {
        currentUserId = userId;
    }

    public static void logout() {
        currentUserId = null;
    }
}
