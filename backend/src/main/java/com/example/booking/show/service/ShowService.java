package com.example.booking.show.service;

import com.example.booking.exception.ConflictException;
import com.example.booking.exception.NotFoundException;
import com.example.booking.movie.Movie;
import com.example.booking.movie.service.MovieService;
import com.example.booking.screen.Screen;
import com.example.booking.screen.service.ScreenService;
import com.example.booking.show.Show;
import com.example.booking.show.dto.ShowRequest;
import com.example.booking.show.dto.ShowResponse;
import com.example.booking.show.repository.ShowRepository;
import com.example.booking.util.PageResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ShowService {

    private static final int CLEANING_BUFFER_MINUTES = 15;
    private static final int MAX_PAGE_SIZE = 50;
    private static final LocalDateTime FAR_FUTURE = LocalDateTime.of(9999, 12, 31, 0, 0);

    private final ShowRepository showRepository;
    private final MovieService movieService;
    private final ScreenService screenService;

    public ShowService(ShowRepository showRepository, MovieService movieService, ScreenService screenService) {
        this.showRepository = showRepository;
        this.movieService = movieService;
        this.screenService = screenService;
    }

    /**
     * READ_COMMITTED so that, after waiting for the screen lock, the overlap check sees
     * shows committed by the transaction that held the lock before us.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShowResponse create(ShowRequest req) {
        // 1. Lock FIRST: concurrent creators for the same screen queue up here.
        Screen screen = screenService.getEntityForUpdate(req.screenId());
        Movie movie = movieService.getEntity(req.movieId());

        LocalDateTime start = req.startTime().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime end = start.plusMinutes(movie.getDurationMinutes() + CLEANING_BUFFER_MINUTES);

        // 2. Check
        if (showRepository.existsOverlap(screen.getId(), start, end)) {
            throw new ConflictException("Another show on this screen overlaps " + start + " to " + end);
        }

        // 3. Insert
        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(start);
        show.setEndTime(end);
        show.setPrice(req.price().setScale(2, RoundingMode.HALF_UP));
        try {
            return ShowResponse.from(showRepository.save(show));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A show already starts at " + start + " on this screen");
        }
    }

    @Transactional(readOnly = true)
    public ShowResponse get(Long id) {
        return ShowResponse.from(showRepository.findDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Show not found: " + id)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ShowResponse> search(Long movieId, Long theatreId, String city, LocalDate date,
                                             int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now;
        LocalDateTime to = FAR_FUTURE;
        if (date != null) {
            LocalDateTime dayStart = date.atStartOfDay();
            from = dayStart.isAfter(now) ? dayStart : now;   // never list shows that already started
            to = date.plusDays(1).atStartOfDay();
        }
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by("startTime", "id"));

        return PageResponse.of(showRepository.search(
                movieId == null ? 0L : movieId,
                theatreId == null ? 0L : theatreId,
                city == null ? "" : city.trim().toLowerCase(),
                from, to, pageable), ShowResponse::from);
    }
}
