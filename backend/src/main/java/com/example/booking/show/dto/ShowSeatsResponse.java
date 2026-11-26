package com.example.booking.show.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ShowSeatsResponse(Long showId, String movieTitle, String screenName,
                                LocalDateTime startTime, BigDecimal price,
                                int totalSeats, int availableSeats,
                                Map<String, List<ShowSeatResponse>> rows) {
}
