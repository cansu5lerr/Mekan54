package com.example.mekan54.repository;

import com.example.mekan54.model.Reservation;
import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserAndDateTime (User user, LocalDateTime dateTime);
    List <Reservation> findByVenueAndDateTime(Venue venue, LocalDateTime dateTime);
}
