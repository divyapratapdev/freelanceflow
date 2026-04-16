package com.freelanceflow.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the authenticated user's ID (stored as Long in the JWT principal).
     * Throws IllegalStateException if no authentication is present.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long id) {
            return id;
        }
        // Fallback: parse string principal (e.g. in tests)
        if (principal instanceof String str) {
            return Long.parseLong(str);
        }
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
    }
}
