package com.example.booking.screen.repository;

import com.example.booking.screen.Screen;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheatreIdOrderByName(Long theatreId);

    boolean existsByTheatreIdAndNameIgnoreCase(Long theatreId, String name);

    /** SELECT ... FOR UPDATE: other transactions locking the same row wait until we commit. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Screen s where s.id = :id")
    Optional<Screen> findByIdForUpdate(@Param("id") Long id);
}
