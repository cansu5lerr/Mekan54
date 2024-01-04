package com.example.mekan54.controller;

import com.example.mekan54.payload.request.FavoriteRequest;
import com.example.mekan54.security.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class FavoriteController {
    @Autowired
    FavoriteService favoriteService;
    @PostMapping("/addFavorite/{venueId}")
    public ResponseEntity<?> addFavorite(@RequestHeader("Authorization") String token, @PathVariable Long venueId) {
        return favoriteService.addFavorite(token,venueId);
    }
    @DeleteMapping("/deleteFavorite/{venueId}")
    public ResponseEntity<?> deleteFavorite(@RequestHeader("Authorization") String token, @PathVariable Long venueId) {
        return favoriteService.deleteFavorite(token,venueId);
    }

}
