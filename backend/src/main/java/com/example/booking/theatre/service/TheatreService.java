package com.example.booking.theatre.service;

import com.example.booking.exception.NotFoundException;
import com.example.booking.theatre.Theatre;
import com.example.booking.theatre.dto.TheatreRequest;
import com.example.booking.theatre.dto.TheatreResponse;
import com.example.booking.theatre.repository.TheatreRepository;
import com.example.booking.util.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TheatreService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @Transactional
    public TheatreResponse create(TheatreRequest req) {
        Theatre t = new Theatre();
        t.setName(req.name().trim());
        t.setCity(req.city().trim());
        t.setAddress(blankToNull(req.address()));
        t.setImageUrl(blankToNull(req.imageUrl()));
        return TheatreResponse.from(theatreRepository.save(t));
    }

    @Transactional(readOnly = true)
    public PageResponse<TheatreResponse> search(String city, int page, int size) {
        String c = city == null ? "" : city.trim().toLowerCase();
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by("name"));
        return PageResponse.of(theatreRepository.search(c, pageable), TheatreResponse::from);
    }

    @Transactional(readOnly = true)
    public TheatreResponse get(Long id) {
        return TheatreResponse.from(getEntity(id));
    }

    /** Used by other modules (Screen) so they never touch TheatreRepository directly. */
    @Transactional(readOnly = true)
    public Theatre getEntity(Long id) {
        return theatreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Theatre not found: " + id));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
