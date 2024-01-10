package com.example.mekan54.security.service;
import com.example.mekan54.model.Favorite;
import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import com.example.mekan54.payload.request.FavoriteRequest;
import com.example.mekan54.repository.FavoriteRepository;
import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.repository.VenueRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FavoriteService {

    @Autowired
    FavoriteRepository favoriteRepository;
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    public ResponseEntity <?> addFavorite (String token , Long venueId) {
        User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
        if(authenticatedUser instanceof User) {
            Optional <Venue> venueOptional = venueRepository.findById(venueId);
            if(venueOptional.isPresent()) {
                Venue venue = venueOptional.get();
                Favorite favorite = createFavorite(authenticatedUser,venue);
                List<Favorite> favoriteList = new ArrayList<>();
                favoriteList.add(favorite);
                venue.setFavorites(favoriteList);
                authenticatedUser.setFavorites(favoriteList);
                saveUserFavoriteAndVenue(authenticatedUser,favorite,venue);
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "Favorilere eklendi!");
                return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
            }
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("error", "Favorilere eklenemedi!");
            return ResponseEntity.badRequest().body(responseMap);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı bulunamadı");
        return ResponseEntity.badRequest().body(responseMap);
    }
    public ResponseEntity<?> deleteFavorite(String token, Long venueId) {
        User authenticatedUser =userDetailsService.getAuthenticatedUserFromToken(token);
        if(authenticatedUser instanceof User){
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if(venueOptional.isPresent()) {
                Venue venue = venueOptional.get();
                Optional<Favorite> favoriteOptional = findFavoriteInVenue(authenticatedUser, venue);
               if(favoriteOptional.isPresent()) {
                   Favorite favorite = favoriteOptional.get();
                   venue.getFavorites().remove(favorite);
                   authenticatedUser.getFavorites().remove(favorite);
                   deleteFavorite(authenticatedUser, venue,favorite);
                   Map<String, String> responseMap = new HashMap<>();
                   responseMap.put("message", "Favorilerden kaldırıldı!");
                   return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
               }
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("error", "Favorilerden kaldırlamadı!");
                return ResponseEntity.badRequest().body(responseMap);
            }
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("error", "Mekan bulunamadı!");
            return ResponseEntity.badRequest().body(responseMap);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Mekan bulunamadı!");
        return ResponseEntity.badRequest().body(responseMap);
    }
    @Transactional
    private void deleteFavorite (User user, Venue venue, Favorite favorite) {
        favoriteRepository.delete(favorite);
        venueRepository.save(venue);
        userRepository.save(user);
    }
    private Favorite createFavorite (User user, Venue venue) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setVenue(venue);

        return favorite;
    }
    @Transactional
    private void saveUserFavoriteAndVenue(User authenticatedUser, Favorite favorite, Venue venue) {
        userRepository.save(authenticatedUser);
        favoriteRepository.save(favorite);
        venueRepository.save(venue);
    }
    private Optional<Favorite> findFavoriteInVenue(User user, Venue venue) {
        return venue.getFavorites().stream()
                .filter(favorite -> favorite.getUser().equals(user))
                .findFirst();
    }




}
