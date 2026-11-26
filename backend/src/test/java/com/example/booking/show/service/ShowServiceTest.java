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
import com.example.booking.theatre.Theatre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 15, 19, 0);

    @Mock ShowRepository showRepository;
    @Mock MovieService movieService;
    @Mock ScreenService screenService;

    private ShowService service;
    private Screen screen;
    private Movie movie;

    @BeforeEach
    void setUp() {
        service = new ShowService(showRepository, movieService, screenService);

        Theatre theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("PVR");
        theatre.setCity("Delhi");

        screen = new Screen();
        screen.setId(10L);
        screen.setName("Screen 1");
        screen.setTheatre(theatre);

        movie = new Movie();
        movie.setId(20L);
        movie.setTitle("Inception");
        movie.setDurationMinutes(148);
    }

    @Test
    void endTimeIncludesMovieDurationAndCleaningBuffer() {
        when(screenService.getEntityForUpdate(10L)).thenReturn(screen);
        when(movieService.getEntity(20L)).thenReturn(movie);
        when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

        ShowResponse response = service.create(new ShowRequest(20L, 10L, START, new BigDecimal("250")));

        assertEquals(START.plusMinutes(148 + 15), response.endTime());
        assertEquals(new BigDecimal("250.00"), response.price());
        assertEquals("ACTIVE", response.status());
    }

    @Test
    void startTimeIsTruncatedToTheMinute() {
        when(screenService.getEntityForUpdate(10L)).thenReturn(screen);
        when(movieService.getEntity(20L)).thenReturn(movie);
        when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(new ShowRequest(20L, 10L, START.withSecond(45), new BigDecimal("100")));

        ArgumentCaptor<Show> captor = ArgumentCaptor.forClass(Show.class);
        verify(showRepository).save(captor.capture());
        assertEquals(START, captor.getValue().getStartTime());
    }

    @Test
    void overlappingShowIsRejectedAndNothingIsSaved() {
        when(screenService.getEntityForUpdate(10L)).thenReturn(screen);
        when(movieService.getEntity(20L)).thenReturn(movie);
        when(showRepository.existsOverlap(eq(10L), eq(START), eq(START.plusMinutes(163)))).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.create(new ShowRequest(20L, 10L, START, new BigDecimal("250"))));

        verify(showRepository, never()).save(any());
    }

    @Test
    void screenIsLockedBeforeAnythingElseIsRead() {
        when(screenService.getEntityForUpdate(10L)).thenReturn(screen);
        when(movieService.getEntity(20L)).thenReturn(movie);
        when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(new ShowRequest(20L, 10L, START, new BigDecimal("250")));

        InOrder order = inOrder(screenService, movieService, showRepository);
        order.verify(screenService).getEntityForUpdate(10L);   // lock first
        order.verify(movieService).getEntity(20L);
        order.verify(showRepository).existsOverlap(eq(10L), any(), any());
    }

    @Test
    void unknownScreenFailsFast() {
        when(screenService.getEntityForUpdate(99L)).thenThrow(new NotFoundException("Screen not found: 99"));

        assertThrows(NotFoundException.class,
                () -> service.create(new ShowRequest(20L, 99L, START, new BigDecimal("250"))));

        verifyNoInteractions(movieService, showRepository);
    }
}
