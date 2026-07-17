package com.mtrxxp.backend.user.dto;

import com.mtrxxp.backend.user.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role,
        LocalDateTime createdDate
) {
}
