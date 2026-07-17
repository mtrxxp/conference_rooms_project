package com.mtrxxp.backend.room.dto;

import com.mtrxxp.backend.room.RoomStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank String name,
        String location,
        @NotNull @Min(1) Integer capacity,
        @NotNull @DecimalMin("0.0") BigDecimal pricePerHour,
        String description,
        RoomStatus roomStatus
) {
}
