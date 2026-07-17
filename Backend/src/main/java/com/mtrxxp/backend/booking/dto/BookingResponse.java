package com.mtrxxp.backend.booking.dto;

import com.mtrxxp.backend.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        String userEmail,
        Long roomId,
        String roomName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        BookingStatus bookingStatus,
        String note,
        LocalDateTime createdDate
) {
}
