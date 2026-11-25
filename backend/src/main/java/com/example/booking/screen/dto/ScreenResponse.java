package com.example.booking.screen.dto;

import com.example.booking.screen.Screen;

public record ScreenResponse(Long id, Long theatreId, String name) {

    public static ScreenResponse from(Screen s) {
        return new ScreenResponse(s.getId(), s.getTheatre().getId(), s.getName());
    }
}
