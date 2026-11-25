package com.example.booking.movie.service;

import com.example.booking.exception.ConflictException;
import com.example.booking.exception.NotFoundException;
import com.example.booking.movie.Movie;
import com.example.booking.movie.dto.MovieRequest;
import com.example.booking.movie.dto.MovieResponse;
import com.example.booking.movie.repository.MovieRepository;
import com.example.booking.util.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class MovieService {

    private static final Set<String> SORTABLE = Set.of("id", "title", "releaseDate", "durationMinutes");
    private static final int MAX_PAGE_SIZE = 50;

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    public MovieResponse create(MovieRequest req) {
        Movie movie = new Movie();
        apply(movie, req);
        return MovieResponse.from(movieRepository.save(movie));
    }

    @Transactional(readOnly = true)
    public MovieResponse get(Long id) {
        return MovieResponse.from(find(id));
    }

    /** Used by other modules (Show) so they never touch MovieRepository directly. */
    @Transactional(readOnly = true)
    public Movie getEntity(Long id) {
        return find(id);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovieResponse> search(String name, String genre, String language, String city,
                                              int page, int size, String sortBy, String direction) {
        String sortField = SORTABLE.contains(sortBy) ? sortBy : "title";
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(dir, sortField));

        Page<Movie> result = movieRepository.search(
                "%" + norm(name) + "%", norm(genre), norm(language), norm(city), pageable);
        return PageResponse.of(result, MovieResponse::from);
    }

    @Transactional
    public MovieResponse update(Long id, MovieRequest req) {
        Movie movie = find(id);
        apply(movie, req);          // no save() needed: dirty checking flushes the change at commit
        return MovieResponse.from(movie);
    }

    @Transactional
    public void delete(Long id) {
        Movie movie = find(id);
        if (movieRepository.hasShows(id)) {
            throw new ConflictException("Movie has shows and cannot be deleted");
        }
        movieRepository.delete(movie);
    }

    private Movie find(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found: " + id));
    }

    private void apply(Movie m, MovieRequest r) {
        m.setTitle(r.title().trim());
        m.setDescription(r.description());
        m.setGenre(blankToNull(r.genre()));
        m.setLanguage(blankToNull(r.language()));
        m.setDurationMinutes(r.durationMinutes());
        m.setReleaseDate(r.releaseDate());
        m.setPosterUrl(blankToNull(r.posterUrl()));
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
