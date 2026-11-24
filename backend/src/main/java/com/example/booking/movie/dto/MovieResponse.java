package com.example.booking.movie.dto;

import com.example.booking.movie.Movie;

import java.time.LocalDate;

public record MovieResponse(Long id, String title, String description, String genre,
                            String language, Integer durationMinutes, LocalDate releaseDate,
                            String posterUrl) {

    public static MovieResponse from(Movie m) {
        return new MovieResponse(m.getId(), m.getTitle(), m.getDescription(), m.getGenre(),
                m.getLanguage(), m.getDurationMinutes(), m.getReleaseDate(), m.getPosterUrl());
    }
}
