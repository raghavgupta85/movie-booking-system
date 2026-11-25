package com.example.booking.seat.dto;

import java.util.List;
import java.util.Map;

public record SeatLayoutResponse(Long screenId, String screenName, int totalSeats,
                                 Map<String, List<SeatResponse>> rows) {
}
