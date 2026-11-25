package com.example.booking.seat.dto;

import com.example.booking.seat.Seat;

public record SeatResponse(Long id, String rowLabel, Integer seatNumber, String seatType) {

    public static SeatResponse from(Seat s) {
        return new SeatResponse(s.getId(), s.getRowLabel(), s.getSeatNumber(), s.getSeatType());
    }
}
