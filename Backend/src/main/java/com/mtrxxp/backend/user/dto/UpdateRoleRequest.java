package com.mtrxxp.backend.user.dto;

import com.mtrxxp.backend.user.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull Role role
) {
}
