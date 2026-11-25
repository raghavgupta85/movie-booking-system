package com.example.booking.theatre.dto;

import com.example.booking.theatre.Theatre;

public record TheatreResponse(Long id, String name, String city, String address, String imageUrl) {

    public static TheatreResponse from(Theatre t) {
        return new TheatreResponse(t.getId(), t.getName(), t.getCity(), t.getAddress(), t.getImageUrl());
    }
}
