package com.example.booking.show.repository;

import com.example.booking.show.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    /** True if an ACTIVE show on this screen overlaps [newStart, newEnd). Back-to-back is allowed. */
    @Query("""
            select count(s) > 0 from Show s
            where s.screen.id = :screenId
              and s.status = 'ACTIVE'
              and s.startTime < :newEnd
              and s.endTime > :newStart
            """)
    boolean existsOverlap(@Param("screenId") Long screenId,
                          @Param("newStart") LocalDateTime newStart,
                          @Param("newEnd") LocalDateTime newEnd);

    @Query("""
            select s from Show s
            join fetch s.movie
            join fetch s.screen sc
            join fetch sc.theatre
            where s.id = :id
            """)
    Optional<Show> findDetailsById(@Param("id") Long id);

    /** Sentinels: movieId/theatreId 0 and city "" mean "no filter". city is lower-cased. */
    @Query(value = """
            select s from Show s
            join fetch s.movie
            join fetch s.screen sc
            join fetch sc.theatre t
            where s.status = 'ACTIVE'
              and s.startTime >= :from and s.startTime < :to
              and (:movieId = 0L or s.movie.id = :movieId)
              and (:theatreId = 0L or t.id = :theatreId)
              and (:city = '' or lower(t.city) = :city)
            """,
            countQuery = """
            select count(s) from Show s
            where s.status = 'ACTIVE'
              and s.startTime >= :from and s.startTime < :to
              and (:movieId = 0L or s.movie.id = :movieId)
              and (:theatreId = 0L or s.screen.theatre.id = :theatreId)
              and (:city = '' or lower(s.screen.theatre.city) = :city)
            """)
    Page<Show> search(@Param("movieId") Long movieId,
                      @Param("theatreId") Long theatreId,
                      @Param("city") String city,
                      @Param("from") LocalDateTime from,
                      @Param("to") LocalDateTime to,
                      Pageable pageable);
}
