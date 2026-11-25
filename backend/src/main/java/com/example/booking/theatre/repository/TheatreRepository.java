package com.example.booking.theatre.repository;

import com.example.booking.theatre.Theatre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    /** city is lower-cased and non-null; "" means "all cities". */
    @Query("select t from Theatre t where (:city = '' or lower(t.city) = :city)")
    Page<Theatre> search(@Param("city") String city, Pageable pageable);
}
