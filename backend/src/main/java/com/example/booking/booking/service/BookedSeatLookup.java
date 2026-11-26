package com.example.booking.booking.service;

import com.example.booking.booking.repository.BookingSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class BookedSeatLookup {

    private final BookingSeatRepository bookingSeatRepository;

    public BookedSeatLookup(BookingSeatRepository bookingSeatRepository) {
        this.bookingSeatRepository = bookingSeatRepository;
    }

    @Transactional(readOnly = true)
    public Set<Long> bookedSeatIds(Long showId) {
        return new HashSet<>(bookingSeatRepository.findActiveSeatIdsByShowId(showId));
    }
}
