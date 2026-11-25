package com.example.booking.theatre.controller;

import com.example.booking.theatre.dto.TheatreRequest;
import com.example.booking.theatre.dto.TheatreResponse;
import com.example.booking.theatre.service.TheatreService;
import com.example.booking.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TheatreResponse create(@Valid @RequestBody TheatreRequest request) {
        return theatreService.create(request);
    }

    @GetMapping
    public PageResponse<TheatreResponse> list(@RequestParam(required = false) String city,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return theatreService.search(city, page, size);
    }

    @GetMapping("/{id}")
    public TheatreResponse get(@PathVariable Long id) {
        return theatreService.get(id);
    }
}
