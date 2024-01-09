package com.example.mekan54.repository;

import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

  

}
