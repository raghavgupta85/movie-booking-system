package com.example.booking.show.service;

import com.example.booking.booking.service.BookedSeatLookup;
import com.example.booking.seat.dto.SeatLayoutResponse;
import com.example.booking.seat.dto.SeatResponse;
import com.example.booking.seat.service.SeatService;
import com.example.booking.show.dto.ShowResponse;
import com.example.booking.show.dto.ShowSeatResponse;
import com.example.booking.show.dto.ShowSeatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ShowSeatService {

    private final ShowService showService;
    private final SeatService seatService;
    private final BookedSeatLookup bookedSeatLookup;

    public ShowSeatService(ShowService showService, SeatService seatService, BookedSeatLookup bookedSeatLookup) {
        this.showService = showService;
        this.seatService = seatService;
        this.bookedSeatLookup = bookedSeatLookup;
    }

    @Transactional(readOnly = true)
    public ShowSeatsResponse getSeats(Long showId) {
        ShowResponse show = showService.get(showId);                       // 404 if the show doesn't exist
        SeatLayoutResponse layout = seatService.getLayout(show.screenId());
        Set<Long> booked = bookedSeatLookup.bookedSeatIds(showId);

        Map<String, List<ShowSeatResponse>> rows = new LinkedHashMap<>();
        int available = 0;
        for (Map.Entry<String, List<SeatResponse>> row : layout.rows().entrySet()) {
            List<ShowSeatResponse> seats = new ArrayList<>();
            for (SeatResponse s : row.getValue()) {
                boolean isBooked = booked.contains(s.id());
                if (!isBooked) {
                    available++;
                }
                seats.add(new ShowSeatResponse(s.id(), s.rowLabel(), s.seatNumber(), s.seatType(),
                        isBooked ? "BOOKED" : "AVAILABLE"));
            }
            rows.put(row.getKey(), seats);
        }
        return new ShowSeatsResponse(show.id(), show.movieTitle(), show.screenName(), show.startTime(),
                show.price(), layout.totalSeats(), available, rows);
    }
}
