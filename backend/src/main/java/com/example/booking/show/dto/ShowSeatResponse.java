package com.example.booking.show.dto;

public record ShowSeatResponse(Long id, String rowLabel, Integer seatNumber, String seatType, String status) {
}
