package com.example.booking.seat.service;

import com.example.booking.exception.ConflictException;
import com.example.booking.screen.Screen;
import com.example.booking.screen.service.ScreenService;
import com.example.booking.seat.Seat;
import com.example.booking.seat.dto.SeatGenerationRequest;
import com.example.booking.seat.dto.SeatLayoutResponse;
import com.example.booking.seat.dto.SeatResponse;
import com.example.booking.seat.repository.SeatRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenService screenService;

    public SeatService(SeatRepository seatRepository, ScreenService screenService) {
        this.seatRepository = seatRepository;
        this.screenService = screenService;
    }

    @Transactional
    public SeatLayoutResponse generate(Long screenId, SeatGenerationRequest req) {
        Screen screen = screenService.getEntity(screenId);   // 404 if missing
        if (seatRepository.existsByScreenId(screenId)) {
            throw new ConflictException("Screen already has a seat layout and it cannot be regenerated");
        }
        String type = req.seatType() == null ? "REGULAR" : req.seatType();

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < req.rows(); r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            for (int n = 1; n <= req.seatsPerRow(); n++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(n);
                seat.setSeatType(type);
                seats.add(seat);
            }
        }

        try {
            return toLayout(screen, seatRepository.saveAll(seats));
        } catch (DataIntegrityViolationException e) {
            // a concurrent generate got there first: unique key (screen_id, row_label, seat_number)
            throw new ConflictException("Screen already has a seat layout and it cannot be regenerated");
        }
    }

    @Transactional(readOnly = true)
    public SeatLayoutResponse getLayout(Long screenId) {
        Screen screen = screenService.getEntity(screenId);
        return toLayout(screen, seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId));
    }

    private SeatLayoutResponse toLayout(Screen screen, List<Seat> seats) {
        Map<String, List<SeatResponse>> rows = new LinkedHashMap<>();
        seats.stream()
                .sorted((a, b) -> a.getRowLabel().equals(b.getRowLabel())
                        ? a.getSeatNumber().compareTo(b.getSeatNumber())
                        : a.getRowLabel().compareTo(b.getRowLabel()))
                .forEach(s -> rows.computeIfAbsent(s.getRowLabel(), k -> new ArrayList<>())
                        .add(SeatResponse.from(s)));
        return new SeatLayoutResponse(screen.getId(), screen.getName(), seats.size(), rows);
    }
}
