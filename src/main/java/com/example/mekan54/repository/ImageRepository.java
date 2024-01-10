package com.example.mekan54.repository;

import com.example.mekan54.model.Image;
import com.example.mekan54.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.venue.id = :venueId")
    void deleteImagesByVenueId(@Param("venueId") Long venueId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.user.id = :userId")
    void deleteImagesByUserId(@Param("userId") Long userId);

}
