package com.example.mekan54.repository;

import com.example.mekan54.model.Image;
import com.example.mekan54.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    void deleteAllByVenue(Venue venue);

}
