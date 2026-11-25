package com.example.booking.screen.repository;

import com.example.booking.screen.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheatreIdOrderByName(Long theatreId);

    boolean existsByTheatreIdAndNameIgnoreCase(Long theatreId, String name);
}
