package com.example.booking.booking.repository;

import com.example.booking.booking.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    /** Seats currently held (PENDING or CONFIRMED) for a show: active is 1, released rows have NULL. */
    @Query("select bs.seatId from BookingSeat bs where bs.showId = :showId and bs.active is not null")
    List<Long> findActiveSeatIdsByShowId(@Param("showId") Long showId);
}
