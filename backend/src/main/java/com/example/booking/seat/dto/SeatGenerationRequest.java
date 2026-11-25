package com.example.booking.seat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SeatGenerationRequest(
        @NotNull @Min(1) @Max(26) Integer rows,
        @NotNull @Min(1) @Max(50) Integer seatsPerRow,
        @Pattern(regexp = "REGULAR|PREMIUM|VIP", message = "must be REGULAR, PREMIUM or VIP") String seatType) {
}
