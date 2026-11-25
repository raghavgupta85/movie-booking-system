package com.example.booking.screen.controller;

import com.example.booking.screen.dto.ScreenRequest;
import com.example.booking.screen.dto.ScreenResponse;
import com.example.booking.screen.service.ScreenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres/{theatreId}/screens")
public class ScreenController {

    private final ScreenService screenService;

    public ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScreenResponse create(@PathVariable Long theatreId, @Valid @RequestBody ScreenRequest request) {
        return screenService.create(theatreId, request);
    }

    @GetMapping
    public List<ScreenResponse> list(@PathVariable Long theatreId) {
        return screenService.listByTheatre(theatreId);
    }
}
