package com.mtrxxp.backend.room.dto;

import com.mtrxxp.backend.room.RoomStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RoomResponse(
        Long id,
        String name,
        String location,
        Integer capacity,
        BigDecimal pricePerHour,
        String description,
        RoomStatus roomStatus,
        LocalDateTime createdDate
) {
}
