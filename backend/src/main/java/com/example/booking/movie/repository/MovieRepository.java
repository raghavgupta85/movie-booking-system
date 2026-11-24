package com.example.booking.movie.repository;

import com.example.booking.movie.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * All parameters are non-null, already lower-cased. "" means "no filter".
     * title is a ready-made LIKE pattern, e.g. "%aven%".
     */
    @Query("""
            select m from Movie m
            where lower(m.title) like :title
              and (:genre = '' or lower(m.genre) = :genre)
              and (:language = '' or lower(m.language) = :language)
              and (:city = '' or exists (
                    select 1 from Show s
                    where s.movie = m and lower(s.screen.theatre.city) = :city))
            """)
    Page<Movie> search(@Param("title") String title,
                       @Param("genre") String genre,
                       @Param("language") String language,
                       @Param("city") String city,
                       Pageable pageable);

    @Query("select count(s) > 0 from Show s where s.movie.id = :movieId")
    boolean hasShows(@Param("movieId") Long movieId);
}
