package com.example.booking.seat.controller;

import com.example.booking.seat.dto.SeatGenerationRequest;
import com.example.booking.seat.dto.SeatLayoutResponse;
import com.example.booking.seat.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/screens/{screenId}/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public SeatLayoutResponse generate(@PathVariable Long screenId,
                                       @Valid @RequestBody SeatGenerationRequest request) {
        return seatService.generate(screenId, request);
    }

    @GetMapping
    public SeatLayoutResponse layout(@PathVariable Long screenId) {
        return seatService.getLayout(screenId);
    }
}
