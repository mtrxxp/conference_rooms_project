package com.mtrxxp.backend.booking.dto;

import com.mtrxxp.backend.booking.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record BookingStatusUpdateRequest(
        @NotNull BookingStatus bookingStatus
) {
}
