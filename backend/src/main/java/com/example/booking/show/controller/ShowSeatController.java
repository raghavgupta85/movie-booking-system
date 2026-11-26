package com.example.booking.show.controller;

import com.example.booking.show.dto.ShowSeatsResponse;
import com.example.booking.show.service.ShowSeatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows/{showId}/seats")
public class ShowSeatController {

    private final ShowSeatService showSeatService;

    public ShowSeatController(ShowSeatService showSeatService) {
        this.showSeatService = showSeatService;
    }

    @GetMapping
    public ShowSeatsResponse seats(@PathVariable Long showId) {
        return showSeatService.getSeats(showId);
    }
}
