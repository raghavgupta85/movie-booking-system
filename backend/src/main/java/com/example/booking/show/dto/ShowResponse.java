package com.example.booking.show.dto;

import com.example.booking.show.Show;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowResponse(Long id, Long movieId, String movieTitle,
                           Long theatreId, String theatreName, String city,
                           Long screenId, String screenName,
                           LocalDateTime startTime, LocalDateTime endTime,
                           BigDecimal price, String status) {

    public static ShowResponse from(Show s) {
        return new ShowResponse(
                s.getId(),
                s.getMovie().getId(), s.getMovie().getTitle(),
                s.getScreen().getTheatre().getId(), s.getScreen().getTheatre().getName(),
                s.getScreen().getTheatre().getCity(),
                s.getScreen().getId(), s.getScreen().getName(),
                s.getStartTime(), s.getEndTime(), s.getPrice(), s.getStatus());
    }
}
