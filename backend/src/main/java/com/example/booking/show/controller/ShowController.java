package com.example.booking.show.controller;

import com.example.booking.show.dto.ShowRequest;
import com.example.booking.show.dto.ShowResponse;
import com.example.booking.show.service.ShowService;
import com.example.booking.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowResponse create(@Valid @RequestBody ShowRequest request) {
        return showService.create(request);
    }

    @GetMapping("/{id}")
    public ShowResponse get(@PathVariable Long id) {
        return showService.get(id);
    }

    @GetMapping
    public PageResponse<ShowResponse> search(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return showService.search(movieId, theatreId, city, date, page, size);
    }
}
