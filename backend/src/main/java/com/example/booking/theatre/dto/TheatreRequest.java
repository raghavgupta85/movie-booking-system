package com.example.booking.theatre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TheatreRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 80) String city,
        @Size(max = 300) String address,
        @Size(max = 500) String imageUrl) {
}
