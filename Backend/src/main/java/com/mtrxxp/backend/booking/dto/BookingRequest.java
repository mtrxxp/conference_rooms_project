package com.mtrxxp.backend.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingRequest(
        @NotNull Long roomId,
        @NotNull @Future LocalDateTime startDate,
        @NotNull @Future LocalDateTime endDate,
        String note
) {
}
