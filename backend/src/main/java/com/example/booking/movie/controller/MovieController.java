package com.example.booking.movie.controller;

import com.example.booking.movie.dto.MovieRequest;
import com.example.booking.movie.dto.MovieResponse;
import com.example.booking.movie.service.MovieService;
import com.example.booking.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse create(@Valid @RequestBody MovieRequest request) {
        return movieService.create(request);
    }

    @GetMapping
    public PageResponse<MovieResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return movieService.search(name, genre, language, city, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public MovieResponse get(@PathVariable Long id) {
        return movieService.get(id);
    }

    @PutMapping("/{id}")
    public MovieResponse update(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return movieService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        movieService.delete(id);
    }
}
