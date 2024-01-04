package com.example.mekan54.repository;

import com.example.mekan54.model.Comment;
import com.example.mekan54.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVenue(Venue venue);
    // diğer sorgular
}