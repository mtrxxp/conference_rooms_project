package com.mtrxxp.backend.auth.dto;

import com.mtrxxp.backend.user.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String email,
        Role role
) {
    public AuthResponse(String token, Long userId, String email, Role role) {
        this(token, "Bearer", userId, email, role);
    }
}
