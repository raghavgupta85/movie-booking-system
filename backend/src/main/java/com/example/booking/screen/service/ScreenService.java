package com.example.booking.screen.service;

import com.example.booking.exception.ConflictException;
import com.example.booking.exception.NotFoundException;
import com.example.booking.screen.Screen;
import com.example.booking.screen.dto.ScreenRequest;
import com.example.booking.screen.dto.ScreenResponse;
import com.example.booking.screen.repository.ScreenRepository;
import com.example.booking.theatre.Theatre;
import com.example.booking.theatre.service.TheatreService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreService theatreService;

    public ScreenService(ScreenRepository screenRepository, TheatreService theatreService) {
        this.screenRepository = screenRepository;
        this.theatreService = theatreService;
    }

    @Transactional
    public ScreenResponse create(Long theatreId, ScreenRequest req) {
        Theatre theatre = theatreService.getEntity(theatreId);   // 404 if the theatre doesn't exist
        String name = req.name().trim();
        if (screenRepository.existsByTheatreIdAndNameIgnoreCase(theatreId, name)) {
            throw new ConflictException("Screen '" + name + "' already exists in this theatre");
        }
        Screen screen = new Screen();
        screen.setTheatre(theatre);
        screen.setName(name);
        try {
            return ScreenResponse.from(screenRepository.save(screen));
        } catch (DataIntegrityViolationException e) {
            // two simultaneous requests: the unique key (theatre_id, name) wins
            throw new ConflictException("Screen '" + name + "' already exists in this theatre");
        }
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> listByTheatre(Long theatreId) {
        theatreService.getEntity(theatreId);
        return screenRepository.findByTheatreIdOrderByName(theatreId).stream()
                .map(ScreenResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Screen getEntity(Long id) {
        return screenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Screen not found: " + id));
    }
}
